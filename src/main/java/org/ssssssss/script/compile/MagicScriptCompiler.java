package org.ssssssss.script.compile;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.asm.*;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.VarIndex;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.VariableSetter;
import org.ssssssss.script.parsing.ast.binary.AssigmentOperation;
import org.ssssssss.script.parsing.ast.statement.VariableAccess;
import org.ssssssss.script.runtime.MagicScriptRuntime;
import org.ssssssss.script.runtime.function.MagicScriptLambdaFunction;
import org.ssssssss.script.runtime.handle.ArithmeticHandle;
import org.ssssssss.script.runtime.handle.BitHandle;
import org.ssssssss.script.runtime.handle.FunctionCallHandle;
import org.ssssssss.script.runtime.handle.OperatorHandle;

import java.lang.invoke.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import static org.ssssssss.script.asm.ClassWriter.COMPUTE_FRAMES;
import static org.ssssssss.script.asm.ClassWriter.COMPUTE_MAXS;
import static org.ssssssss.script.compile.Descriptor.make_descriptor;

/**
 * 脚本编译器
 */
public class MagicScriptCompiler implements Opcodes {

	private static final AtomicLong COUNTER = new AtomicLong(1);

	private final ClassWriter classWriter;

	private final Long id = COUNTER.getAndIncrement();

	/**
	 * < <= == === != !=== >= > 等操作符处理器
	 */
	private static final Handle OPERATOR_HANDLE = makeHandle(OperatorHandle.class);

	/**
	 * << >> >>> & ^ | 运算
	 */
	private static final Handle BIT_HANDLE = makeHandle(BitHandle.class);

	/**
	 * 方法调用、lambda调用、函数调用处理器
	 */
	private static final Handle FUNCTION_HANDLE = makeHandle(FunctionCallHandle.class);

	/**
	 * + - * / % 处理器
	 */
	private static final Handle ARITHMETIC_HANDLE = makeHandle(ArithmeticHandle.class);

	private final Stack<MethodVisitor> methodVisitors = new Stack<>();

	private final Stack<List<String>> vars = new Stack<>();

	private final Stack<Label[]> labelStack = new Stack<>();

	private final Stack<List<Node>> finallyStack = new Stack<>();

	private final Set<VarIndex> varIndices;

	/**
	 * -1 ~ 5的int值
	 */
	private static final int[] ICONST = {ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};

	private final List<Span> spans;

	private int functionIndex = 0;

	private int tempIndex = 4;

	public MagicScriptCompiler(Set<VarIndex> varIndices, List<Span> spans) {
		this.varIndices = varIndices;
		this.spans = spans;
		// 创建类并继承 MagicScriptRuntime
		classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
		classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, getClassName(), null, getJvmType(MagicScriptRuntime.class), null);
		classWriter.visitSource(getClassName() + ".ms", null);
		createMethod(ACC_PUBLIC, "<init>", make_descriptor(void.class));
		this.load0()
				.invoke(INVOKESPECIAL, MagicScriptRuntime.class, "<init>", void.class)
				.insn(RETURN)
				.pop();
		// 创建execute方法
		createMethod(ACC_PUBLIC, "execute", make_descriptor(Object.class, MagicScriptContext.class));
	}

	/**
	 * 创建方法
	 *
	 * @param access     访问属性
	 * @param methodName 方法名
	 * @param descriptor 方法描述
	 */
	public MagicScriptCompiler createMethod(int access, String methodName, String descriptor) {
		MethodVisitor visitor = classWriter.visitMethod(access, methodName, descriptor, null, null);
		visitor.visitCode();
		methodVisitors.push(visitor);
		vars.push(new ArrayList<>());
		finallyStack.push(null);
		labelStack.push(new Label[2]);
		return this;
	}

	public int getTempIndex() {
		return tempIndex++;
	}

	/**
	 * 获取lambda函数下标
	 */
	public int getFunctionIndex() {
		return ++functionIndex;
	}

	/**
	 * 标识continue位置
	 */
	public MagicScriptCompiler start(Label label) {
		labelStack.peek()[0] = label;
		return this;
	}

	/**
	 * 标识end位置
	 */
	public MagicScriptCompiler end(Label label) {
		labelStack.peek()[1] = label;
		return this;
	}

	/**
	 * 跳转到continue位置
	 */
	public MagicScriptCompiler start() {
		return jump(GOTO, labelStack.peek()[0]);
	}

	/**
	 * 跳转到break位置
	 */
	public MagicScriptCompiler end() {
		return jump(GOTO, labelStack.peek()[1]);
	}

	/**
	 * 访问AST节点
	 */
	public MagicScriptCompiler visit(Node node) {
		// 对于赋值语句的特殊处理
		if (node instanceof AssigmentOperation) {
			AssigmentOperation operation = (AssigmentOperation) node;
			// 如果是a = b = 1这种情况，则需要读取a的值
			if (operation.getLeftOperand() instanceof VariableAccess) {
				return compile(node, true).compile(operation.getLeftOperand());
			}
		}
		return compile(node, false);
	}

	/**
	 * 编译AST节点
	 */
	public MagicScriptCompiler compile(Node node) {
		return compile(node, false);
	}

	/**
	 * 编译AST节点
	 *
	 * @param node AST节点
	 * @param pop  是否需要弹出栈顶
	 */
	public MagicScriptCompiler compile(Node node, boolean pop) {
		if (node == null) {
			return insn(ACONST_NULL);
		} else {
			int index = spans.indexOf(node.getSpan()) + 1;
			if (index > 0) {
				MethodVisitor mv = _this();
				Label label = new Label();
				// 设置行号
				mv.visitLabel(label);
				mv.visitLineNumber(index, label);
			}
			// 编译该节点
			node.compile(this);
		}
		// 对于赋值语句的特殊处理，因为赋值语句有两种
		// 不带返回值的： a+=1
		// 带返回值的 xxx.xx = 1
		if (node instanceof AssigmentOperation) {
			AssigmentOperation operation = (AssigmentOperation) node;
			if (operation.getLeftOperand() instanceof VariableSetter && operation.getLeftOperand() instanceof VariableAccess) {
				return this;
			}
		}
		// 对于带有返回值的表达式，且需要弹出栈顶时，追加指令POP
		return pop && node instanceof Expression ? insn(POP) : this;
	}

	public MagicScriptCompiler tryCatch(Label start, Label end, Label handle, Class<?> target) {
		_this().visitTryCatchBlock(start, end, handle, getJvmType(target));
		return this;
	}

	public MagicScriptCompiler tryCatch(Label start, Label end, Label handle, List<Node> finallyBlock) {
		finallyStack.push(finallyBlock);
		_this().visitTryCatchBlock(start, end, handle, getJvmType(null));
		return this;
	}

	/**
	 * 访问
	 */
	public MagicScriptCompiler visit(List<? extends Node> nodes) {
		nodes.forEach(this::visit);
		return this;
	}

	/**
	 * 编译
	 */
	public MagicScriptCompiler compile(List<? extends Node> nodes) {
		nodes.forEach(it -> compile(it, true));
		return this;
	}

	/**
	 * 加载this
	 */
	public MagicScriptCompiler load0() {
		_this().visitVarInsn(ALOAD, 0);
		return this;
	}

	/**
	 * 加载context
	 */
	public MagicScriptCompiler load1() {
		_this().visitVarInsn(ALOAD, 1);
		return this;
	}

	/**
	 * 加载context
	 */
	public MagicScriptCompiler newArrayList() {
		return this.typeInsn(NEW, ArrayList.class)
				.insn(DUP)
				.invoke(INVOKESPECIAL, ArrayList.class, "<init>", void.class);
	}

	/**
	 * 加载vars
	 */
	public MagicScriptCompiler load2() {
		_this().visitVarInsn(ALOAD, 2);
		return this;
	}

	/**
	 * 加载3号变量，一般指异常(临时变量)
	 */
	public MagicScriptCompiler load3() {
		_this().visitVarInsn(ALOAD, 3);
		return this;
	}

	/**
	 * 加载变量（从数组中）
	 */
	public MagicScriptCompiler aaload(int index) {
		return load2().visitInt(index).insn(AALOAD);
	}

	/**
	 * 加载变量（从数组中）
	 */
	public MagicScriptCompiler load(int index) {
		_this().visitVarInsn(ALOAD, index);
		return this;
	}

	/**
	 * 加载属性
	 */
	public MagicScriptCompiler getField(Class<?> owner, String name, Class<?> type) {
		_this().visitFieldInsn(GETFIELD, getJvmType(owner), name, Type.getDescriptor(type));
		return this;
	}

	/**
	 * 加载变量
	 */
	public MagicScriptCompiler load(VarIndex varIndex) {
		return aaload(varIndex.getIndex());
	}

	/**
	 * 加载变量
	 *
	 * @param name 变量名
	 */
	public MagicScriptCompiler load(String name) {
		int index = vars.peek().indexOf(name) + 1;
		if (index > 0) {    // 如果当前栈中有，则直接使用
			return aaload(index);
		} else {
			// 从环境中获取
			this.load1()
					.ldc(name)
					.invoke(INVOKEVIRTUAL, MagicScriptContext.class, "getEnvironmentValue", Object.class, String.class);
		}
		return this;
	}

	public MagicScriptCompiler label(Label label) {
		_this().visitLabel(label);
		if (label == finallyStack.peek()) {
			finallyStack.pop();
		}
		return this;
	}

	/**
	 * 跳转
	 */
	public MagicScriptCompiler jump(int opcode, Label label) {
		_this().visitJumpInsn(opcode, label);
		return this;
	}

	/**
	 * 移除变量
	 */
	public MagicScriptCompiler remove(VarIndex varIndex) {
		if (varIndex == null) {
			return this;
		}
		return remove(varIndex.getName());
	}

	/**
	 * 移除变量
	 */
	public MagicScriptCompiler remove(String name) {
		List<String> varList = vars.peek();
		int index = varList.indexOf(name);
		if (index > -1) {
			varList.set(index, null);
		}
		return this;
	}

	/**
	 * 配合pre_store使用，保存至数组中
	 */
	public MagicScriptCompiler store() {
		return insn(AASTORE);
	}

	/**
	 * 保存变量
	 */
	public MagicScriptCompiler store(int index) {
		_this().visitVarInsn(ASTORE, index);
		return this;
	}

	/**
	 * 写变量前的准备
	 */
	public MagicScriptCompiler pre_store(String name) {
		List<String> varList = vars.peek();
		int index = varList.indexOf(name) + 1;
		if (index == 0) {
			varList.add(name);
			index = varList.size();
		}
		_this().visitVarInsn(ASTORE, 1);
		return visitInt(index);
	}

	/**
	 * 写变量前的准备
	 */
	public MagicScriptCompiler pre_store(int index) {
		return load2().visitInt(index);
	}

	/**
	 * 写变量前的准备
	 */
	public MagicScriptCompiler pre_store(VarIndex varIndex) {
		return pre_store(varIndex.getIndex());
	}

	public MagicScriptCompiler bipush(int value) {
		_this().visitIntInsn(BIPUSH, value);
		return this;
	}

	public MagicScriptCompiler typeInsn(int opcode, Class<?> target) {
		_this().visitTypeInsn(opcode, getJvmType(target));
		return this;
	}


	/**
	 * 二元运算
	 *
	 * @param methodName 运算方法
	 */
	public MagicScriptCompiler operator(String methodName) {
		_this().visitInvokeDynamicInsn(methodName, MethodType.genericMethodType(2).toMethodDescriptorString(), OPERATOR_HANDLE, 2);
		return this;
	}

	/**
	 * 位运算
	 *
	 * @param methodName 运算方法
	 */
	public MagicScriptCompiler bit(String methodName) {
		_this().visitInvokeDynamicInsn(methodName, MethodType.genericMethodType(2).toMethodDescriptorString(), BIT_HANDLE, 2);
		return this;
	}

	/**
	 * 将方法转为lambda
	 *
	 * @param methodName 方法名
	 */
	public MagicScriptCompiler lambda(String methodName) {
		Handle metaFactory = new Handle(H_INVOKESTATIC, getJvmType(LambdaMetafactory.class), "metafactory", make_descriptor(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class), false);
		String descriptor = make_descriptor(Object.class, MagicScriptContext.class, Object[].class);
		Handle impl = new Handle(H_INVOKEVIRTUAL, getClassName(), methodName, descriptor, false);
		_this().visitInvokeDynamicInsn("apply", "(L" + getClassName() + ";)" + Type.getType(MagicScriptLambdaFunction.class).getDescriptor(), metaFactory, Type.getType(descriptor), impl, Type.getType(descriptor));
		return this;
	}

	/**
	 * invokedynamic调用
	 *
	 * @param methodName 方法名
	 * @param arguments  参数个数
	 */
	public MagicScriptCompiler call(String methodName, int arguments) {
		_this().visitInvokeDynamicInsn(methodName, MethodType.genericMethodType(arguments).toMethodDescriptorString(), FUNCTION_HANDLE, arguments);
		return this;
	}

	/**
	 * 执行算术运算
	 *
	 * @param methodName 方法名
	 */
	public MagicScriptCompiler arithmetic(String methodName) {
		_this().visitInvokeDynamicInsn(methodName, MethodType.genericMethodType(2).toMethodDescriptorString(), ARITHMETIC_HANDLE, 2);
		return this;
	}

	/**
	 * 将int值装箱
	 */
	public MagicScriptCompiler asInteger() {
		return invoke(INVOKESTATIC, Integer.class, "valueOf", Integer.class, int.class);
	}

	/**
	 * 将boolean值装箱
	 */
	public MagicScriptCompiler asBoolean() {
		return invoke(INVOKESTATIC, Boolean.class, "valueOf", Boolean.class, boolean.class);
	}

	/**
	 * 调用方法
	 *
	 * @param opcode        调用类型
	 * @param target        目标类
	 * @param method        方法名
	 * @param returnType    返回值类型
	 * @param argumentTypes 参数类型
	 */
	public MagicScriptCompiler invoke(int opcode, Class<?> target, String method, Class<?> returnType, Class<?>... argumentTypes) {
		return invoke(opcode, target, method, false, returnType, argumentTypes);
	}

	/**
	 * 调用方法
	 *
	 * @param opcode        调用类型
	 * @param target        目标类
	 * @param method        方法名
	 * @param isInterface   是否是接口
	 * @param returnType    返回值类型
	 * @param argumentTypes 参数类型
	 */
	public MagicScriptCompiler invoke(int opcode, Class<?> target, String method, boolean isInterface, Class<?> returnType, Class<?>... argumentTypes) {
		_this().visitMethodInsn(opcode, getJvmType(target), method, make_descriptor(returnType, argumentTypes), isInterface);
		return this;
	}

	public MagicScriptCompiler ldc(Object value) {
		_this().visitLdcInsn(value);
		return this;
	}

	public MagicScriptCompiler insn(int opcode) {
		_this().visitInsn(opcode);
		return this;
	}

	public MagicScriptCompiler intInsn(int opcode, int operand) {
		_this().visitIntInsn(opcode, operand);
		return this;
	}

	/**
	 * 编译数组
	 */
	public MagicScriptCompiler newArray(List<Expression> values) {
		int size = values.size();
		visitInt(size).typeInsn(ANEWARRAY, Object.class);
		for (int i = 0; i < size; i++) {
			insn(DUP).visitInt(i).visit(values.get(i)).insn(AASTORE);
		}
		return this;
	}

	/**
	 * 编译int值
	 */
	public MagicScriptCompiler visitInt(int value) {
		if (value >= -1 && value <= 5) {
			insn(ICONST[value + 1]);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			intInsn(BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			intInsn(SIPUSH, value);
		} else {
			ldc(value);
		}
		return this;
	}

	public void loadVars() {
		// var2 = context.createVariables(this, varIndices.size())
		this.load1()
				.load0()
				.visitInt(varIndices.size())
				.invoke(INVOKEVIRTUAL, MagicScriptContext.class, "createVariables", Object[].class, MagicScriptRuntime.class, int.class)
				.store(2);
		// 对于未定义变量值的，从环境中获取
		// var2[xx] = super.getEnvironmentValue(varIndex.getName())
		this.varIndices.stream().filter(VarIndex::isReference).forEach(varIndex -> this.load2()
				.visitInt(varIndex.getIndex())
				.load1()
				.ldc(varIndex.getName())
				.invoke(INVOKEVIRTUAL, MagicScriptContext.class, "getEnvironmentValue", Object.class, String.class)
				.store()
		);
	}

	public List<Node> finallyBlock() {
		return finallyStack.peek();
	}

	public MagicScriptCompiler putFinallyBlock(List<Node> finallyBlock) {
		finallyStack.push(finallyBlock);
		return this;
	}

	public List<Node> getFinallyBlock() {
		return finallyStack.pop();
	}

	public MagicScriptCompiler pop() {
		finallyStack.pop();
		MethodVisitor visitor = methodVisitors.pop();
		visitor.visitInsn(ACONST_NULL);
		visitor.visitInsn(ARETURN);
		visitor.visitMaxs(0, 0);
		visitor.visitEnd();
		vars.pop();
		labelStack.pop();
		return this;
	}

	public byte[] bytecode() {
		pop();
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}


	/**
	 * 获取类名
	 */
	public String getClassName() {
		return "MagicScript_" + id;
	}

	private MethodVisitor _this() {
		return methodVisitors.peek();
	}

	private static String getJvmType(Class<?> target) {
		return target == null ? null : target.getName().replace(".", "/");
	}

	private static Handle makeHandle(Class<?> target) {
		return new Handle(H_INVOKESTATIC,
				getJvmType(target),
				"bootstrap",
				make_descriptor(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, int.class),
				false);
	}
}

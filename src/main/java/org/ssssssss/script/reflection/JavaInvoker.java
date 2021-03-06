package org.ssssssss.script.reflection;

import org.ssssssss.script.convert.ClassImplicitConvert;
import org.ssssssss.script.parsing.Scope;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class JavaInvoker<T extends Executable> {

	private final Map<Integer, ClassImplicitConvert> converts = new HashMap<>();
	private T executable;
	private Class<?>[] parameterTypes;
	private boolean implicit = false;
	private boolean extension = false;
	private Object defaultTarget;

	JavaInvoker(T executable) {
		this.executable = executable;
		this.executable.setAccessible(true);
		this.parameterTypes = this.executable.getParameterTypes();
	}

	public boolean isImplicit() {
		return implicit;
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}

	public boolean isExtension() {
		return extension;
	}

	public void setExtension(boolean extension) {
		this.extension = extension;
	}

	public Object getDefaultTarget() {
		return defaultTarget;
	}

	public void setDefaultTarget(Object defaultTarget) {
		this.defaultTarget = defaultTarget;
	}

	public T getExecutable() {
		return this.executable;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public boolean isVarArgs() {
		return this.executable.isVarArgs();
	}

	public Object invoke0(Object target, Scope scope, Object... arguments) throws Throwable {
		try {
			if (scope != null) {
				Scope.setTempScope(scope);
			}
			if (extension) {
				int argumentLength = arguments == null ? 0 : arguments.length;
				Object[] parameters = new Object[argumentLength + 1];
				if (argumentLength > 0) {
					System.arraycopy(arguments, 0, parameters, 1, argumentLength);
				}
				parameters[0] = target;
				if (target.getClass().isArray()) {
					Object[] objs = new Object[Array.getLength(target)];
					for (int i = 0, len = objs.length; i < len; i++) {
						Array.set(objs, i, Array.get(target, i));
					}
					parameters[0] = objs;
				}
				arguments = parameters;
			} else if (isVarArgs() && parameterTypes.length == 1 && arguments.length == 1 && (arguments[0] == null || arguments[0].getClass().isArray())){
				return invoke(target, arguments);
			}
			return invoke(target, processArguments(arguments));
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (IllegalAccessException e1) {
			throw e1;
		} finally {
			if(scope != null){
				Scope.removeTempScope();
			}
		}
	}

	abstract Object invoke(Object target, Object... arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException;

	/**
	 * ?????????????????????????????????
	 *
	 * @param index                ??????
	 * @param classImplicitConvert ????????????
	 */
	protected void addClassImplicitConvert(int index, ClassImplicitConvert classImplicitConvert) {
		converts.put(index, classImplicitConvert);
	}

	/**
	 * ??????????????????????????????????????????
	 */
	protected Object[] processArguments(Object[] arguments) {
		if (isVarArgs()) {
			int count = this.executable.getParameterCount();
			Object[] args = new Object[count];
			if (arguments != null) {
				for (int i = 0; i < count - 1; i++) {
					args[i] = arguments[i];
				}
				if (arguments.length - count + 1 > 0) {
					int len = arguments.length - count + 1;
					Object varArgs = Array.newInstance(this.executable.getParameterTypes()[count - 1].getComponentType(), len);
					for (int i = 0; i < len; i++) {
						Array.set(varArgs, i, arguments[count - 1 + i]);

					}
					args[count - 1] = varArgs;
				}
			}
			arguments = args;
		}
		if (arguments != null) {
			for (Map.Entry<Integer, ClassImplicitConvert> entry : converts.entrySet()) {
				int index = entry.getKey();
				arguments[index] = entry.getValue().convert(arguments[index], parameterTypes[index]);
			}
		}
		return arguments;
	}
}

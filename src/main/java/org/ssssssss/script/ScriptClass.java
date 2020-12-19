package org.ssssssss.script;

import org.ssssssss.script.annotation.Comment;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ScriptClass {

	private String className;

	private Set<ScriptMethod> methods = new HashSet<>();

	private List<ScriptAttribute> attributes = new ArrayList<>();

	private String superClass;

	private List<String> interfaces = new ArrayList<>();

	private Object[] enums;

	public Object[] getEnums() {
		return enums;
	}

	public void setEnums(Object[] enums) {
		this.enums = enums;
	}

	public void addAttribute(ScriptAttribute attribute) {
		this.attributes.add(attribute);
	}

	public void addMethod(ScriptMethod method) {
		this.methods.add(method);
	}

	public void addInterface(String interfaceName) {
		this.interfaces.add(interfaceName);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Set<ScriptMethod> getMethods() {
		return methods;
	}

	public void setMethods(Set<ScriptMethod> methods) {
		this.methods = methods;
	}

	public String getSuperClass() {
		return superClass;
	}

	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public List<ScriptAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ScriptAttribute> attributes) {
		this.attributes = attributes;
	}

	public static class ScriptAttribute {

		private String type;

		private String name;

		public ScriptAttribute(String type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}
	}

	public static class ScriptMethod {

		private String name;

		private String returnType;

		private String comment;

		private boolean origin;

		private List<ScriptMethodParameter> parameters = new ArrayList<>();

		public ScriptMethod(Method method) {
			this.name = method.getName();
			Comment methodComment = method.getAnnotation(Comment.class);
			comment = methodComment == null ? null : methodComment.value();
			origin = methodComment != null && methodComment.origin();
			Class<?> returnType = method.getReturnType();
			this.returnType = returnType.isArray() ? returnType.getSimpleName() : returnType.getName();
			Parameter[] parameters = method.getParameters();
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					this.parameters.add(new ScriptMethodParameter(parameters[i]));
				}
			}
		}

		public String getComment() {
			return comment;
		}

		public String getName() {
			return name;
		}

		public boolean isOrigin() {
			return origin;
		}

		public String getReturnType() {
			return returnType;
		}

		public List<ScriptMethodParameter> getParameters() {
			return parameters;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ScriptMethod that = (ScriptMethod) o;
			return Objects.equals(name, that.name)  &&
					Objects.equals(parameters, that.parameters);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, parameters);
		}
	}

	public static class ScriptMethodParameter {

		private String name;

		private String type;

		private boolean varArgs;

		private String comment;

		public ScriptMethodParameter(Parameter parameter) {
			this.name = parameter.getName();
			Class<?> type = parameter.getType();
			this.varArgs = parameter.isVarArgs();
			Comment parameterComment = parameter.getAnnotation(Comment.class);
			this.comment = parameterComment == null ? null : parameterComment.value();
			this.type = type.isArray() ? type.getSimpleName() : type.getName();
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public boolean isVarArgs() {
			return varArgs;
		}

		public String getComment() {
			return comment;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ScriptMethodParameter that = (ScriptMethodParameter) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, type);
		}
	}
}

package org.ssssssss.script;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ScriptClass {

	private String className;

	private List<ScriptMethod> methods = new ArrayList<>();

	private List<ScriptAttribute> attributes = new ArrayList<>();

	private String superClass;

	private List<String> interfaces = new ArrayList<>();

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

	public List<ScriptMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<ScriptMethod> methods) {
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

	static class ScriptAttribute {

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

	static class ScriptMethod {

		private String name;

		private String returnType;

		private List<ScriptMethodParameter> parameters = new ArrayList<>();

		public ScriptMethod(Method method) {
			this.name = method.getName();
			Class<?> returnType = method.getReturnType();
			this.returnType = returnType.isArray() ? returnType.getSimpleName() : returnType.getName();
			Parameter[] parameters = method.getParameters();
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					this.parameters.add(new ScriptMethodParameter(parameters[i]));
				}
			}
		}

		public String getName() {
			return name;
		}

		public String getReturnType() {
			return returnType;
		}

		public List<ScriptMethodParameter> getParameters() {
			return parameters;
		}
	}

	static class ScriptMethodParameter {

		private String name;

		private String type;

		public ScriptMethodParameter(Parameter parameter) {
			this.name = parameter.getName();
			Class<?> type = parameter.getType();
			this.type = type.isArray() ? type.getSimpleName() : type.getName();
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}
	}
}

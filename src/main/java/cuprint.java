import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.plaf.synth.SynthSplitPaneUI;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class cuprint {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// creates an input stream for the file to be parsed

		String code = "";
		HashMap<String, Boolean> classOrIntList = new HashMap<String, Boolean>();
		ArrayList<CompilationUnit> cuList = new ArrayList<CompilationUnit>();
		ArrayList<String> useCasesList = new ArrayList<String>();
		HashMap<String, String> associationMap = createAssociationMap();
		HashMap<String, String> multiplicityMap = new HashMap<String, String>();
		final File folder = new File(args[0]);
		for (final File f : folder.listFiles()) {
			FileInputStream in = new FileInputStream(f);
			CompilationUnit cu;
			try {
				cu = JavaParser.parse(in);
				cuList.add(cu);

			} finally {
				in.close();
			}

		}

		for (CompilationUnit cu : cuList) {
			List<TypeDeclaration> cl = cu.getTypes();
			for (Node var : cl) {
				ClassOrInterfaceDeclaration classOrInterface = (ClassOrInterfaceDeclaration) var;
				classOrIntList.put(classOrInterface.getName(), classOrInterface.isInterface());
			}
		}
		for (CompilationUnit cu : cuList) {
			ArrayList<String> publicParamList = new ArrayList<String>();
			ArrayList<String> publicParamListNames = new ArrayList<String>();
			// parse the file
			// code = code + "[" +
			// f.getName().substring(0,f.getName().indexOf(".")) + "|";
			List<TypeDeclaration> cl = cu.getTypes();
			for (Node var : cl) {
				ClassOrInterfaceDeclaration classOrInterface = (ClassOrInterfaceDeclaration) var;
				if (classOrInterface.isInterface()) {
					code = code + "[" + "<<interface>>;" + classOrInterface.getName() + "]";

				} else {

					code = code + "[" + classOrInterface.getName();

				}
				int x = 0;
				int y = 0;
				String fieldCode = "";
				String methodCode = "";
				// handling for Methods
				for (BodyDeclaration t : ((TypeDeclaration) var).getMembers()) {
					if (t instanceof MethodDeclaration) {
						if ("PUBLIC"
								.equalsIgnoreCase(ModifierSet.getAccessSpecifier(((MethodDeclaration) t).getModifiers())
										.toString())
								|| "PRIVATE".equalsIgnoreCase(ModifierSet
										.getAccessSpecifier(((MethodDeclaration) t).getModifiers()).toString())) {
							MethodDeclaration methodDeclaration = ((MethodDeclaration) t);
							String methodAccesModifier = ModifierSet
									.getAccessSpecifier(((MethodDeclaration) t).getModifiers()).toString();
							String methodReturn = methodDeclaration.getType().toString() + "  ";
							String methodName = methodDeclaration.getName();
							String parameters = "(";

							String methodVar = methodDeclaration.getType().toString() + "  ";
							methodVar = methodVar + methodDeclaration.getName();

							if (methodDeclaration.getParameters().size() != 0) {
								for (Parameter param : methodDeclaration.getParameters()) {
									parameters = parameters + param.getId() + " : " + param.getType() + ",";
								}
								parameters = parameters.substring(0, parameters.length() - 1) + ")";
							} else {
								parameters = parameters + ")";
							}

							methodVar = methodAccesModifier + "" + methodName + parameters + ":" + methodReturn;
							methodVar = methodVar.replace("PRIVATE", "-");
							methodVar = methodVar.replace("PUBLIC", "+");
							methodVar = methodVar.replace("[", "   (");
							methodVar = methodVar.replace("]", ")");
							methodVar = methodVar.replace("<", "(");
							methodVar = methodVar.replace(">", ")");
							if (x == 0) {
								methodCode = methodCode + "|";
								x = 1;
							}
							if (methodDeclaration.getName().toString().startsWith("get")
									|| methodDeclaration.getName().toString().startsWith("set")) {
								if (ModifierSet.getAccessSpecifier(((MethodDeclaration) t).getModifiers()).toString()
										.equals("PUBLIC")) {
									String publicVarName = "";
									String publicVarType = "";
									if (methodDeclaration.getName().toString().startsWith("set")) {
										if (((methodDeclaration.getParameters()).size() != 0)) {
											publicVarType = ((Parameter) (methodDeclaration.getParameters().get(0)))
													.getType().toString();
											for (Object blockstmt : methodDeclaration.getChildrenNodes()) {
												if (blockstmt instanceof BlockStmt) {
													for (Object exprstmt : ((Node) blockstmt).getChildrenNodes()) {
														if (exprstmt instanceof ExpressionStmt) {
															for (Object assignExpr : ((Node) exprstmt)
																	.getChildrenNodes()) {
																if (assignExpr instanceof AssignExpr) {
																	for (Object fieldAccessExpr : ((Node) assignExpr)
																			.getChildrenNodes()) {
																		if (fieldAccessExpr instanceof FieldAccessExpr) {
																			FieldAccessExpr fieldAccessExpr1 = (FieldAccessExpr) fieldAccessExpr;
																			publicVarName = fieldAccessExpr1.getField()
																					.toString();
																			break;
																		} else
																			if (fieldAccessExpr instanceof NameExpr) {
																			NameExpr nameExpr1 = (NameExpr) fieldAccessExpr;
																			publicVarName = nameExpr1
																					.toStringWithoutComments();
																			break;
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
										String param1 = ModifierSet
												.getAccessSpecifier(((MethodDeclaration) t).getModifiers()).toString()
												+ " " + publicVarName + ":" + publicVarType;
										param1 = param1.replace("PUBLIC", "+");
										param1 = param1.replace("PRIVATE", "-");
										publicParamList.add(param1);
										publicParamListNames.add(publicVarName);
									}
								}
							} else {
								methodCode = methodCode + methodVar + ";";
								for (Object param : methodDeclaration.getChildrenNodes()) {
									if (param instanceof Parameter) {
										String usesClassName = (((Parameter) param).getType()).toString();
										if (classOrIntList.containsKey(usesClassName)) {
											if (classOrIntList.get(usesClassName)) {
												if (classOrInterface.isInterface()) {
													useCasesList.add("[<<interface>>;" + classOrInterface.getName()
															+ "]" + "uses -.->[<<interface>>;" + usesClassName + "]");
												} else {
													useCasesList.add("[" + classOrInterface.getName() + "]"
															+ "uses -.->[<<interface>>;" + usesClassName + "]");
												}
											} else {
												if (classOrInterface.isInterface()) {
													useCasesList.add("[<<interface>>;" + classOrInterface.getName()
															+ "]" + "uses -.->[" + usesClassName + "]");
												} else {
													useCasesList.add("[" + classOrInterface.getName() + "]"
															+ "uses -.->[" + usesClassName + "]");
												}
											}
										}
									}
									// handling method body
									if (param instanceof BlockStmt) {

										for (Object exprstmt : ((Node) param).getChildrenNodes()) {
											if (exprstmt instanceof ExpressionStmt) {
												for (Object v : ((Node) exprstmt).getChildrenNodes()) {
													if (v instanceof VariableDeclarationExpr) {
														String usesClassName = ((VariableDeclarationExpr) (v)).getType()
																.toStringWithoutComments();
														if (classOrIntList.containsKey(usesClassName)) {
															if (classOrIntList.get(usesClassName)) {
																if (classOrInterface.isInterface()) {
																	useCasesList.add("[<<interface>>;"
																			+ classOrInterface.getName() + "]"
																			+ "uses -.->[<<interface>>;" + usesClassName
																			+ "]");
																} else {
																	useCasesList.add("[" + classOrInterface.getName()
																			+ "]" + "uses -.->[<<interface>>;"
																			+ usesClassName + "]");
																}
															} else {
																if (classOrInterface.isInterface()) {
																	useCasesList.add("[<<interface>>;"
																			+ classOrInterface.getName() + "]"
																			+ "uses -.->[" + usesClassName + "]");
																} else {
																	useCasesList.add("[" + classOrInterface.getName()
																			+ "]" + "uses -.->[" + usesClassName + "]");
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}

					// constructor adding chalu

					if (t instanceof ConstructorDeclaration) {
						if ("PUBLIC"
								.equalsIgnoreCase(ModifierSet
										.getAccessSpecifier(((ConstructorDeclaration) t).getModifiers()).toString())
								|| "PRIVATE".equalsIgnoreCase(ModifierSet
										.getAccessSpecifier(((ConstructorDeclaration) t).getModifiers()).toString())) {
							ConstructorDeclaration ConstructorDeclaration = ((ConstructorDeclaration) t);
							String methodAccesModifier = ModifierSet
									.getAccessSpecifier(((ConstructorDeclaration) t).getModifiers()).toString();
							String methodName = ConstructorDeclaration.getName();
							String parameters = "(";
							if (ConstructorDeclaration.getParameters().size() != 0) {
								for (Parameter param : ConstructorDeclaration.getParameters()) {
									parameters = parameters + param.getId() + " : " + param.getType() + ",";
								}
								parameters = parameters.substring(0, parameters.length() - 1) + ")";
							} else {
								parameters = parameters + ")";
							}

							String methodVar = methodAccesModifier + "" + methodName + parameters;
							methodVar = methodVar.replace("PRIVATE", "-");
							methodVar = methodVar.replace("PUBLIC", "+");
							methodVar = methodVar.replace("[", "   (");
							methodVar = methodVar.replace("]", ")");
							methodVar = methodVar.replace("<", "(");
							methodVar = methodVar.replace(">", ")");
							if (x == 0) {
								methodCode = methodCode + "|";
								x = 1;
							}
							methodCode = methodCode + methodVar + ";";
							for (Object param : ConstructorDeclaration.getChildrenNodes()) {
								if (param instanceof Parameter) {
									String usesClassName = (((Parameter) param).getType()).toString();
									if (classOrIntList.containsKey(usesClassName)) {
										if (classOrIntList.get(usesClassName)) {
											if (classOrInterface.isInterface()) {
												useCasesList.add("[<<interface>>;" + classOrInterface.getName() + "]"
														+ "uses -.->[<<interface>>;" + usesClassName + "]");
											} else {
												useCasesList.add("[" + classOrInterface.getName() + "]"
														+ "uses -.->[<<interface>>;" + usesClassName + "]");
											}
										} else {
											if (classOrInterface.isInterface()) {
												useCasesList.add("[<<interface>>;" + classOrInterface.getName() + "]"
														+ "uses -.->[" + usesClassName + "]");
											} else {
												useCasesList.add("[" + classOrInterface.getName() + "]" + "uses -.->["
														+ usesClassName + "]");
											}
										}
									}
								}
							}
						}
					}
					// constructor adding khatam
				}
				// Handling for Fields
				for (BodyDeclaration t : ((TypeDeclaration) var).getMembers()) {
					if (t instanceof FieldDeclaration) {
						if ("PUBLIC"
								.equalsIgnoreCase(ModifierSet.getAccessSpecifier(((FieldDeclaration) t).getModifiers())
										.toString())
								|| "PRIVATE".equalsIgnoreCase(ModifierSet
										.getAccessSpecifier(((FieldDeclaration) t).getModifiers()).toString())) {
							String instanceVar = t.toStringWithoutComments();
							if (!publicParamListNames
									.contains(((FieldDeclaration) (t)).getVariables().get(0).toString())) {
								instanceVar = ModifierSet.getAccessSpecifier(((FieldDeclaration) t).getModifiers())
										.toString() + " " + ((FieldDeclaration) (t)).getVariables().get(0) + ":"
										+ ((FieldDeclaration) (t)).getType() + ";";
								// String instanceVar =
								instanceVar = instanceVar.replace("PRIVATE", "-");
								instanceVar = instanceVar.replace("PUBLIC", "+");
								instanceVar = instanceVar.replace("protected", "#");
								instanceVar = instanceVar.replace("<", "(");
								instanceVar = instanceVar.replace(">", ")");
								instanceVar = instanceVar.replace("[]", "(*)");
								if (y == 0) {
									fieldCode = fieldCode + "|";
									y = 1;
								}

								String dep = ((FieldDeclaration) (t)).getType().toString().replace("Collection<", "");
								dep = dep.replace(">", "");
								dep = dep.replace("[]", "");
								if (classOrIntList.containsKey(dep)) {

									String dependency;
									String operation;
									instanceVar = "";
									// fieldCode = fieldCode.substring(0,
									// fieldCode.length() - 1);
									if (((FieldDeclaration) t).getChildrenNodes().get(0).toString()
											.contains("Collection<")) {
										if (classOrIntList.get(dep)) {

											dependency = "[" + classOrInterface.getName() + "]" + "-" + "["
													+ "<<interface>>;" + dep + "]";
											operation = "0..*";
										} else {
											dependency = "[" + classOrInterface.getName() + "]" + "-" + "[" + dep + "]";
											operation = "0..*";
										}
									} else {
										if (classOrIntList.get(dep)) {
											dependency = "[" + classOrInterface.getName() + "]" + "-" + "["
													+ "<<interface>>;" + dep + "]";
											operation = "1";
										} else {
											dependency = "[" + classOrInterface.getName() + "]" + "-" + "[" + dep + "]";
											operation = "1";
										}
									}

									/*
									 * if(!(useCasesList.contains(
									 * dependencyReverse) )){
									 * useCasesList.add(dependency); }
									 */
									multiplicityMap.put(dependency, operation);
								}
								fieldCode = fieldCode + instanceVar;
							}
						}
					}
				}

				for (String instance : publicParamList) {
					fieldCode = fieldCode + instance + ";";
				}

				/* code=code+"|"; */
				code = code + "|" + fieldCode + methodCode + "]";
				if (null != ((ClassOrInterfaceDeclaration) (var)).getExtends()) {
					code = code + "," + ((ClassOrInterfaceDeclaration) (var)).getExtends() + "^-" + "["
							+ classOrInterface.getName() + "]";
				}
				if (null != ((ClassOrInterfaceDeclaration) (var)).getImplements()) {
					List<ClassOrInterfaceType> interfaceList = (List<ClassOrInterfaceType>) ((ClassOrInterfaceDeclaration) (var))
							.getImplements();
					for (ClassOrInterfaceType intface : interfaceList) {
						code = code + "," + "[" + "<<interface>>;" + intface + "]" + "^-.-" + "["
								+ classOrInterface.getName() + "]";

					}
				}
			}

			/*
			 * for(int i=0; i<((TypeDeclaration) var).getMembers().size(); i++){
			 * code = code +"|" }
			 */
			code = code + ",";
			// visit and print the methods names

		}
		// Code for adding multiplicity dependencies
		ArrayList<String> reverseStringList = new ArrayList<String>();
		Iterator iter = multiplicityMap.keySet().iterator();
		while (iter.hasNext()) {
			String mm = (String) iter.next();
			String firstClass = mm.substring(0, mm.indexOf("-"));
			String SecondClass = mm.substring(mm.indexOf("-") + 1, mm.length());
			String reverseString = SecondClass + associationMap.get("link") + firstClass;
			if (multiplicityMap.containsKey(reverseString)) {
				reverseStringList.add(reverseString);
				String depMult = firstClass + multiplicityMap.get(reverseString) + associationMap.get("link")
						+ multiplicityMap.get(mm) + SecondClass;
				useCasesList.add(depMult);
				iter.remove();
			}

		}
		iter = multiplicityMap.keySet().iterator();
		while (iter.hasNext()) {
			String mm = (String) iter.next();
			if (!reverseStringList.contains(mm)) {
				String firstClass = mm.substring(0, mm.indexOf("-"));
				String SecondClass = mm.substring(mm.indexOf("-") + 1, mm.length());
				String depMult = firstClass + associationMap.get("link") + multiplicityMap.get(mm) + SecondClass;
				useCasesList.add(depMult);
			}

		}
		Set<String> hs = new HashSet<String>();
		hs.addAll(useCasesList);
		useCasesList.clear();
		useCasesList.addAll(hs);

		for (String s : useCasesList) {
			code = code + "," + s;
		}
		code = code.replace("||", "|");
		code = code.replace("||", "|");
		code = code.replace("|]", "]");
		System.out.println("code" + code);
		code = "http://yuml.me/diagram/plain/class/" + code + ".png";
		GraphicGenerator.getPNGFile(code, args[1]);
		System.out.println("Output file is generated sucessfully");
	}

	private static HashMap<String, String> createAssociationMap() {
		HashMap<String, String> associationHashMap = new HashMap<String, String>();
		associationHashMap.put("many", "*");
		associationHashMap.put("one", "1");
		associationHashMap.put("blank", "");
		associationHashMap.put("link", "-");
		return associationHashMap;
	}

}
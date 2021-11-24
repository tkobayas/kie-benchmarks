package org.drools.benchmarks.common.providers;


public class FunctionUtilGen {

    private static int NUM_FUNC = 50;
    
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("package org.drools.benchmarks.common.providers;\n");
        sb.append("public class FunctionUtil {\n");

        for (int i = 0; i < NUM_FUNC; i++) {
            sb.append("  public static int myFunction" + i + "() {\n");
            sb.append("    return " + i + ";\n");
            sb.append("  }\n");
        }
        sb.append("}");
        System.out.println(sb.toString());
    }
}

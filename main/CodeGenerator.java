package hw2;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.io.File;

public class CodeGenerator {
    public static void main(String[] args) {
        // 读取文件
        if (args.length == 0) {
            System.err.println("請輸入文件名");
            return;
        }
        String fileName = args[0];

        try {
            FileInputAndRead mermaidCode = new FileInputAndRead(fileName);
            mermaidCode.readFile();
        } catch (IOException e) {
            System.err.println("無法讀取文件 " + fileName);
            e.printStackTrace();
            return;
        }

    }
}

class FileInputAndRead {
    private String filePath;

    public FileInputAndRead(String filePath) {
        this.filePath = filePath;
    }

    void readFile() throws IOException {
        BufferedWriter writer = null;
        File newFile = null;
        int classAmount = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                line = ModifiedString.modify(line);

                if (line.contains("class ")){
                    classAmount++;
                    if(classAmount>1){
                        //classAmount = 0;
                        writer.write("}\n");
                        writer.flush();
                        writer.close();
                    }
                    String[] tokens = line.split("\\s+");
                    String className = tokens[1];
                    newFile = new File(className + ".java");
                    writer = new BufferedWriter(new FileWriter(newFile));
                }
                String content = Parser.analysis(line);
                if (!content.isEmpty()) {
                    writer.write(content + "\n");
                    writer.flush();
                }
                if(line.trim().isEmpty()){
                }
            }
            writer.write("}");
        } catch (FileNotFoundException e) {
            System.err.println("文件未找到: " + filePath);
            throw e;
        } catch (IOException e) {
            System.err.println("无法读取文件 " + filePath);
            throw e;
        }finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

class Parser {
    private static String[] classList = new String[10000];
    private static int size = 0;
    public static String analysis(String line) {

        if (("classDiagram".equals(line))){
            return "";
        }

        String strDetect = "class ";
        boolean containsTheclassWord = line.contains(strDetect);
        
        if (containsTheclassWord) {
            String className = null;
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.equals("class")) {
                    className = tokenizer.nextToken();
                }
                break;
            }
            if (className != null){
                classList[size++] = className;
            }
            return "public "+line + " {";
        }

        int x=size-1;
        boolean containsClassWord = line.contains(classList[x]);
        line = line.trim();
        String[] words = line.split("\\s+");
        line = String.join(" ", words);
        String[] parts = line.split(" ", -1); // 设置拆分次数为-1
        StringBuilder methodSignatureBuilder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            methodSignatureBuilder.append(parts[i]);
            if (i < parts.length - 1) {
                methodSignatureBuilder.append(" ");
            }
        }

        String adjustStringFormat = methodSignatureBuilder.toString();
        char[] charArray = adjustStringFormat.toCharArray();
        StringBuilder typeAndParameter = new StringBuilder();
        int classListLength = classList[x].length() + 4;

        if (classListLength < charArray.length) {
            for (int i = classListLength; i < charArray.length; i++) {
                typeAndParameter.append(charArray[i]);
            }
        }

        String stringOfTypeAndParameter = typeAndParameter.toString();
        
        if(containsClassWord==true){
            //String methodSignature = spiltTypeAndMethod.takeParts(stringOfTypeAndParameter);
            boolean plusOrMinus = publicOrPrivate.judge(line, classList, x);
            if(stringOfTypeAndParameter.startsWith("get")){
                String Form1 = getter.getterOutput(stringOfTypeAndParameter, plusOrMinus);
                return Form1;
            }
            else if(stringOfTypeAndParameter.startsWith("set")){
                String Form2 = setter.setterOutput(stringOfTypeAndParameter, plusOrMinus);
                return Form2;
            }
            else if(stringOfTypeAndParameter.contains(")")&&stringOfTypeAndParameter.contains("(")){
                String Form3 = normal.normalOutput(stringOfTypeAndParameter, plusOrMinus);
                return Form3;
            }
            else{
                if(plusOrMinus == true){
                    return "    public "+stringOfTypeAndParameter+";";
                }
                else{
                    return "    private "+stringOfTypeAndParameter+";";
                }
            }
        }
        return "";
    }    
}

class ModifiedString{
    public static String modify(String line){
        line = line.trim();
        String[] words = line.split("\\s+");
        line = String.join(" ", words);
        String[] parts = line.split(" ", -1);
        StringBuilder methodSignatureBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            methodSignatureBuilder.append(parts[i]);
            if (i < parts.length - 1) {
                methodSignatureBuilder.append(" ");
            }
        }

        String modifiedString = methodSignatureBuilder.toString();
        line = modifiedString.replaceAll("\\s+", " ");
        if(!line.contains(" : ")){
            if(line.contains(": ")){
                line = line.replace(": ", " : ");
            }
            if(line.contains(" :")){
                line = line.replace(" :", " : ");
            }
            if(line.contains(":")){
                line = line.replace(":", " : ");
            }
        }
        if(line.endsWith("void")){
            if(!line.endsWith(" void")){
                line = line.replace("void", " void");
            }
        }
        if(line.endsWith("boolean")){
            if(!line.endsWith(" boolean")){
                line = line.replace("boolean", " boolean");
            }
        }
        if(line.endsWith("String")){
            if(!line.endsWith(" String")){
                line = line.replace("String", " String");
            }
        }
        if(line.endsWith("int")){
            if(!line.endsWith(" int")){
                line = line.replace("int", " int");
            }
        }


        
        line = line.replaceAll("\\s*,\\s*", ", ");
        
        line = line.replaceAll("\\+\\s*", "+");
        line = line.replaceAll("-\\s*", "-");
        
        line = line.replaceAll("\\s*\\(\\s*", "(");
        line = line.replaceAll("\\s*\\)", ")");
        return line;
    }
}

class getMethodName{
    public static String extractMethodName(String methodSignature){
        int startIndex = methodSignature.indexOf('(');
        String methodName = methodSignature.substring(3, startIndex);
        String methodNameToLowerCase = methodName.toLowerCase();
        return methodNameToLowerCase;
    }
}

class spiltTypeAndMethod{
    public static String takeParts(String stringOfTypeAndParameter){
        stringOfTypeAndParameter = stringOfTypeAndParameter.replaceFirst("\\s+\\S+$", "");
        return stringOfTypeAndParameter;
    }
}

class publicOrPrivate{
    public static boolean judge(String line, String[] classList, int x){
        char[] charArray = line.toCharArray();
        int length = classList[x].length() + 3;
        if(charArray[length] == '-'){
            return false;
        }
        else if(charArray[length] == '+'){
            return true;
        }
        else{
            return false;
        }
    }
}

class getter{
    public static String getterOutput(String getterForm, boolean plusOrMinus){
        String type = null;
        String sign = null;
        String methodSignature = spiltTypeAndMethod.takeParts(getterForm);
        String methodName = getMethodName.extractMethodName(methodSignature);

        if(getterForm.endsWith("String")){
            type = "String";
        }
        else if(getterForm.endsWith("int")){
            type = "int";
        }
        else if(getterForm.endsWith("void")){
            type = "void";
        }
        else if(getterForm.endsWith("boolean")){
            type = "boolean";
        }
        else{
            type = "void";
        }

        if(plusOrMinus == true){
            sign = "public";
        }
        else{
            sign = "private";
        }

        return "    "+sign+" "+type+" "+methodSignature+" {\n"+"        return "+methodName+";\n"+"    }";
    }
}

class setter{
    public static String setterOutput(String setterForm, boolean plusOrMinus){
        String type = null;
        String sign = null;
        String methodSignature = spiltTypeAndMethod.takeParts(setterForm);
        String methodName = getMethodName.extractMethodName(methodSignature);

        if(setterForm.endsWith("String")){
            type = "String";
        }
        else if(setterForm.endsWith("int")){
            type = "int";
        }
        else if(setterForm.endsWith("void")){
            type = "void";
        }
        else if(setterForm.endsWith("boolean")){
            type = "boolean";
        }
        else{
            type = "void";
        }

        if(plusOrMinus == true){
            sign = "public";
        }
        else{
            sign = "private";
        }

        return "    "+sign+" "+type+" "+methodSignature+" {\n"+"        this."+methodName+" = "+methodName+";\n"+"    }";
    }
}

class normal{
    public static String normalOutput(String normalForm, boolean plusOrMinus){
        String type = null;
        String returnType = null;
        String sign = null;
        String methodSignature = spiltTypeAndMethod.takeParts(normalForm);
        //String methodName = getMethodName.extractMethodName(methodSignature);
        if(normalForm.endsWith("String")){
            type = "String";
            returnType = "\"\"";
        }
        else if(normalForm.endsWith("int")){
            type = "int";
            returnType = "0";
        }
        else if(normalForm.endsWith("void")){
            type = "void";
            returnType = "void";
        }
        else if(normalForm.endsWith("boolean")){
            type = "boolean";
            returnType = "false";
        }
        else {
            type = "void";
            returnType = "void";
        }
        if(plusOrMinus == true){
            sign = "public";
        }
        else{
            sign = "private";
        }
        if(!returnType.equals("void")){
            return "    "+sign+" "+type+" "+methodSignature+" {return "+returnType+";}";
        }
        else{
            return "    "+sign+" "+type+" "+methodSignature+" {;}";
        }
    }
}
# Compile
javac -sourcepath src -d out src/DictionaryApp.java

# Build the jar
jar cfe SlangDictionary.jar DictionaryApp -C out .

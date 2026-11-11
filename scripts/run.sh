# Run the jar file if it exists
if [ -f SlangDictionary.jar ]; then
    java -jar SlangDictionary.jar

# Run the built 'out' otherwise
elif [ -d out ]; then
    java -cp out DictionaryApp

# If none exist, run the 'build.sh' script and run the jar file
else
    ./scripts/build.sh && java -jar SlangDictionary.jar
fi

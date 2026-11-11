# Slang Dictionary App

A Java console application for looking up slang, managing definitions, viewing
search history, and playing two multiple-choice quizzes. The initial dictionary
comes from `slang.txt`; changes are saved separately.

## Requirements

- JDK 10 or newer (`java`, `javac`, and `jar` on your PATH)
- A POSIX-compatible shell for the `.sh` scripts, such as Git Bash on Windows

Run the commands below from the project root, where `slang.txt` is located.

## Build and run

```sh
./scripts/build.sh
./scripts/run.sh
```

`build.sh` compiles `src/DictionaryApp.java` and its dependencies into `out/`,
then creates `SlangDictionary.jar`. `run.sh` runs the JAR if it exists,
otherwise runs the compiled classes in `out/`. If neither exists, it builds the
JAR first. Run `build.sh` again after changing the source so the JAR contains
your latest code.

You can also compile and run the application directly:

```sh
javac -sourcepath src -d out src/DictionaryApp.java
java -cp out DictionaryApp
```

## Menu options

| Option | Action                                               |
| ------ | ---------------------------------------------------- |
| 1      | Find definitions by slang word and record the search |
| 2      | Find slang words whose definitions contain a keyword |
| 3      | View or clear search history                         |
| 4      | Add a slang word or update an existing one           |
| 5      | Edit a slang word or its definitions                 |
| 6      | Delete a slang word                                  |
| 7      | Restore the dictionary from `slang.txt`              |
| 8      | Show the slang of the day                            |
| 9      | Quiz: choose the meaning of a slang word             |
| 10     | Quiz: choose the slang word matching a meaning       |

Enter any other menu choice to exit.

## Data files

`slang.txt` is the original source. Its first line is `Slag` followed by a
backtick and `Meaning`. Each later entry uses a backtick between the slang word
and its definitions, with `|` between multiple definitions:

```text
Slag`Meaning
example`first meaning|second meaning
```

On startup, the app loads `slang.dat` if it is available. Otherwise, it reads
`slang.txt` and creates `slang.dat`. Search history is stored in `history.dat`.
Options that change the dictionary update `slang.dat`; restoring the original
list rebuilds it from `slang.txt`. The `.dat` files and `out/` are ignored by
Git.

## Benchmark

```sh
./scripts/benchmark.sh
```

The script compiles `test/BenchmarkTest.java` and times the core operation
behind each of the ten menu options. It copies existing `slang.dat` and
`history.dat` into a temporary directory before loading, so benchmark changes do
not modify your saved files. If `slang.dat` is absent, it loads from
`slang.txt`. The quiz measurements cover question generation without interactive
input. Each operation runs once, so the reported times are a quick comparison
rather than a statistical performance study.

To benchmark a different source file, pass its path as an argument. The
benchmark looks for `slang.dat` and `history.dat` beside that file:

```sh
./scripts/benchmark.sh path/to/slang.txt
```

## Project layout

- `src/DictionaryApp.java`: console menu and application entry point
- `src/Module/`: dictionary storage, operations, and quiz logic
- `src/Helper/`: console input/output, entry parsing, and timing
- `src/Data/`: shared constants and question type
- `test/BenchmarkTest.java`: standalone benchmark
- `scripts/`: build, run, and benchmark scripts

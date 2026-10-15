# The README demo. Re-record from the repository root with:
#   vhs docs/demo/kite.tape
# Needs: `kite` on PATH, `bat` (for the colored listing), PostgreSQL on
# localhost:5432 with user and password `postgres` (state is written to a fresh
# schema), and network access to GitHub releases. Nothing touches your real
# ~/.kite: kite runs with its home in /tmp/kite-demo.

Output docs/demo/kite.gif

Require kite
Require bat

Set Shell "bash"
Set FontSize 15
Set Width 1100
Set Height 660
Set Padding 24
Set Theme "Catppuccin Mocha"
Set TypingSpeed 35ms
Set WindowBar Colorful

Env JAVA_OPTS "-Duser.home=/tmp/kite-demo"

Hide
Type "export DEMO=$PWD/docs/demo PS1='$ ' && rm -rf /tmp/kite-demo && mkdir -p /tmp/kite-demo && cd /tmp/kite-demo && clear"
Enter
Show

# A new project with two cloud providers
Type "kite new demo -y -p aws,azure"
Sleep 400ms
Enter
Sleep 4s

# Providers download in parallel, one live line each
Type "cd demo && kite providers install"
Sleep 400ms
Enter
Sleep 12s

# Local state, the example file, and only aws for the run (the azure 0.2.0 release
# loads no types: kitecorp/kite-providers#22)
Hide
Type `kite config state -e dev --url "jdbc:postgresql://localhost:5432/postgres?currentSchema=kitedemo_$(date +%s)" --username postgres --password postgres > /dev/null && sed -i '' '/name: azure/,+1d' kitefile.yml && cp $DEMO/hello.kite . && alias cat='bat --paging=never --style=plain --language=kotlin' && clear`
Enter
Sleep 2s
Show

# Kite code: resources, variables, interpolation, a dependency
Type "cat hello.kite"
Sleep 400ms
Enter
Sleep 4s

Type "kite plan -f hello.kite"
Sleep 400ms
Enter
Sleep 6s

Type "kite apply -f hello.kite -y"
Sleep 400ms
Enter
Sleep 6s

Type "cat hello.txt notes.txt"
Sleep 400ms
Enter
Sleep 3s

# Nothing left to do: the state matches the code
Type "clear && kite plan -f hello.kite"
Sleep 400ms
Enter
Sleep 6s

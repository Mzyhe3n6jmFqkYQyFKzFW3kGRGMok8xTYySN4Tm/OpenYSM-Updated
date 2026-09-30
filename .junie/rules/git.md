# Git Commit Policy

When you successfully complete a coding task:

1. Run the relevant tests or verification commands.
2. If the changes are correct and the tests pass, create a Git commit.
3. Do not commit if there are no changes.
4. Do not commit if tests are failing.
5. Use a concise Conventional Commit message.

For commits created by Junie, use Junie as the Git author for that commit:

git -c user.name="Junie" -c user.email="<junie's github email, not human user>" -c commit.gpgsign=false commit -m "<message>"

The human user should not be added as a co-author unless explicitly requested.

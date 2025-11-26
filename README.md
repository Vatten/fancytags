# FancyTags

hello


## Fetch Template Changes
[From answer on stackoverflow](https://stackoverflow.com/questions/37471740/how-to-copy-commits-from-one-git-repo-to-another)
```
git remote add baserad https://github.com/Vatten/baserad
git remote update
git cherry-pick <sha-of-commit>
git push origin main
git remote remove baserad
```
CURRENT_NAME=`git config --global user.name`
CURRENT_EMAIL=`git config --global user.email`
CURRENT_DIR=$PWD
DIR_NAME=`basename "$PWD"`
TMP_DIR=/tmp/sync/$DIR_NAME

# Configuring git author
git config --global user.name 'ntxdev'
git config --global user.email 'hello@ntxdev.com.br'

# Creating fake directory
mkdir -p $TMP_DIR
cp -R . $TMP_DIR
cd $TMP_DIR

# Cloning existing repo
rm -rf .git
git clone git@github.com:institutotim/$DIR_NAME.git _new_repo
mv _new_repo/.git .git
rm -rf _new_repo
git add -A .
git commit -m "Release `date +%m-%d-%y`"
git push origin master

# Restoring default configuration
git config --global user.name $CURRENT_NAME
git config --global user.email $CURRENT_EMAIL
rm -rf $TMP_DIR
cd $CURRENT_DIR

#!/bin/sh

set -xe

mkdir ~/.mallow
cp ./Mallow.jar ~/.mallow
cp -r ./std ~/.mallow
echo "export PATH='~/.mallow:$PATH'" >> ~/.bashrc
echo "export MALLOW_PATH='$HOME/.mallow'" >> ~/.bashrc
echo "java -jar $MALLOW_PATH/Mallow.jar \$1" > mallow
chmod +x mallow
mv mallow ~/.mallow
echo "source ~/.bashrc or restart shell"

work=~/test.tmp
rm -rf $work
mkdir -p $work
cd $work
git clone https://github.com/devizer/jlab.git
# svn checkout https://github.com/devizer/jlab.git
cd jlab
ant
cd out/all
bash win-fast-tests.cmd

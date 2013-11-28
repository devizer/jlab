work=~/test.tmp
rm -rf $work
mkdir -p $work
cd $work
svn checkout https://sandbox.repositoryhosting.com/svn/sandbox_sandbox/javalab --username vladimir
cd javalab
ant
cd out/all
bash win-fast-tests.cmd

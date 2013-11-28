sudo apt-get install patch
set -x
cd ~
cd Desktop
cd vmware-tools-distrib/lib/modules/source
tar xf vmhgfs.tar
wget https://raw.github.com/rasa/vmware-tools-patches/master/patches/vmhgfs/vmhgfs-uid-gid-kernel-3.12-tools-9.6.1.patch
patch -p0 <vmhgfs-uid-gid-kernel-3.12-tools-9.6.1.patch
mv vmhgfs.tar vmhgfs.orig.tar
tar cf vmhgfs.tar vmhgfs-only
cd ~
cd Desktop
cd vmware-tools-distrib
sudo perl ./vmware-install.pl
set +x
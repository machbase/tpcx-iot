# TPCx-IoT Testsuite

These are source codes of testing TPCx-IoT. It consists;
* tpcx-iot : testsuite for TPCx-IoT
* ycsb : Yahoo Clound Serving Benchmark. `tpcx-iot` is based on this testsuite.

If you want to build custom test jar file, you should consider `ycsb` directory. But, otherwise, you may need only `tpcx-iot` directory.

## Preparation (on local testing)
1. Enter `tpcx-iot` directory.
2. Edit `driver_host_list.txt` like this;
```
your_username@localhost
```
3. (If you don't have `clush` command) Install `clustershell`.   
   Here is instruction of CentOS 6, but you can google it to install this in other OS.
```bash
sudo yum-config-manager --add-repo http://dl.fedoraproject.org/pub/epel/6/x86_64/
sudo yum --nogpgcheck install clustershell -y
```
4. Add group only for `your_username@localhost` by typing this;
```bash
sudo echo "all: your_username@localhost" > /etc/clustershell/groups
```
5. Check your ssh connection through loopback is successfully done without no password prompt:  
   If it goes failed, you must register your SSH public key contents (e.g. `$HOME/.ssh/*.pub`) into `$HOME/.ssh/authorized_keys`.  
   *I don't believe it* but if you have no idea for generating SSH key... please google it.
```bash
ssh your_username@localhost
```  
6. (If you're now connected to root, reconnect to your own user! and)  
   Test clustershell is working:
```bash
clush -a date
```  
Below must be displayed.  
```
your_username@localhost: Wed May 29 17:39:52 KST 2019
```

## Preparation (on clustered testing)
Same as 'local testing', except you can describe all hosts in `/etc/clustershell/groups`
For more information, see `tpcx-iot/USER_GUIDE.txt` or [clustershell doc](https://clustershell.readthedocs.io/en/latest/)

## Testing
Just run `tpcx-iot/TPCx-IoT-master.sh`. That's it!

## Troubleshooting
If you encounter any problem, please issue it.

# YCSB Build
In order to do that, you should install 'apache-maven'. Here is instruction of CentOS 6.
```bash
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install apache-maven
```
than, type package command:
```bash
cd ycsb
mvn -U package
```

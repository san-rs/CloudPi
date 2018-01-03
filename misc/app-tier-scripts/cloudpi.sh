# location /etc/profile.d
# so these environment variables will be set at start up of the instance

export JAVA_HOME=/usr/lib/jvm/java-1.7.0
export M2_HOME=/home/ec2-user/apache-maven-3.3.9
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
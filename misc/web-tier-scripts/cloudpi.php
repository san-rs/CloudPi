#location - /var/www/html

<?php
include('Net/SSH2.php');
include('Crypt/RSA.php');

set_time_limit(0);

if($_GET['input']){
$input = $_GET['input'];
}

$key = new Crypt_RSA();
$key->loadKey(file_get_contents('/home/ec2-user/cloudpi-cloud/c-pi/aws-educate-account.pem'));

$ssh = new Net_SSH2('127.0.0.1', 22, 10000000000);
if (!$ssh->login('ec2-user', $key)) {
    exit('Login Failed');
}
$output = $ssh->exec("java -cp /home/ec2-user/cloudpi-cloud/c-pi/target/cloudpi-0.0.1-SNAPSHOT.jar webtier.CloudPiWebTier $input");
echo "<pre>$output</pre>";
?>
<?php /* Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau. */ ?>
<?php
	if(is_resource(@fsockopen('localhost', 50005))) {

		include("../lib/Java.inc");
		$server = java("ServerInterface")->getInstance();


			for($i=400; $i<600; $i++) {
				try {
					$cid = "" . new Java("java.lang.String", $server->login('team' . $i,'team' . $i));
					echo '<font color="#f3f1eb">team ' . $i . ' successfully logged in with CID: ' . $cid . '!</font><br/>';
				} catch (JavaException $exception) { echo '<font color="#FF0000">team ' . $i . ' couldnt log in!</font><br/>'; }
			}//end for(...)
	} else {
		echo "<div style='color:#000000;text-align:center;'>Java bridge could not be established!</div>";
	}
?>

<html>
<title>PC^2 Login Test</title>
<link href="../Team/tab_style/style.css" rel="stylesheet" type="text/css" />
</html>

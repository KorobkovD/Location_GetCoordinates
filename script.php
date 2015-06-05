<?php
header('Content-Type: text/html; charset=windows-1251');
if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
	$str = "Ваше местоположение на ".date("r").": ";
	if (!empty($_POST['Latitude']))
	{
		$str = $str . ' Широта: ' . $_POST['Latitude'];
	}
	if (!empty($_POST['Longitude']))
	{
		$str = $str .' Долгота: '. $_POST['Longitude'];
	}
	if (!empty($_POST['Comment']))
	{
		$str = $str .'. Ваш комментарий: '. $_POST['Comment'];
	}
	$str = $str.'. Скопируйте для использования в Картах: '.$_POST['Latitude'].' '.$_POST['Longitude'];
	// Имя файла.
	$f = 'results/' . 'text' . uniqid() . '.txt';
	file_put_contents($f, $str);
}
?>
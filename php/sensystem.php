<?php

$login = filter_input(INPUT_POST, "login");
$password = filter_input(INPUT_POST, "password");
$data = filter_input(INPUT_POST, "data");


echo "Выгрузка данных : \n";
echo "----------------\n\n";
echo "login : $login\n";
echo "password : $password\n\n";

if (isset($data)){
    echo "Содержание JSON (поле data)\n";
    echo "---------------------------\n\n";
    $json = json_decode($data,true);

    foreach (array_keys($json) as $tableName){
        echo "table $tableName\n";
        echo "--------------------------\n";
        $table = $json[$tableName];

        foreach ($table as $values){
            foreach (array_keys($values) as $field_name){
                echo "$field_name = ".$values[$field_name]." ;";
            }
            echo "\n";
        }
        echo "\n\n";

    }
}


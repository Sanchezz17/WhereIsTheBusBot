# Как настроить локально БД

- Для начала естественно нужно [скачать сервер postgresql](
 https://www.enterprisedb.com/postgresql-tutorial-resources-training?cid=437)
- Далее устанавливаем его. Будет окошко с чекбоксами - проверь, что поставлены все галочки.
- Введи пароль и запиши, он понадобится
- Выбери порт и запомни (либо оставь дефолтный, но тоже запомни, вроде по дефолту всегда 5432)
- Ждем пока установится
- Теперь можете открыть pyAdmin и порадоваться, что у вас есть аналог ssms только в браузере :)
- Добавляем в PATH путь до каталога bin postgres'а. У меня, например, такой 
 ```C:\Program Files\PostgreSQL\13\bin```
- Далее в директории PostgresScripts редактируем файл create.bat. А именно вместо ??? ставим свои данные: пароль, порт
- В файле application.yaml указываем в параметре spring.datasource.url правильный порт, например, jdbc:postgresql://localhost:5432/WhereIsTheTrolleybusOrTramBot,
 в параметре spring.datasource.password свой пароль
- Открываем консольку в этой папке и запускаем create.bat. Следим, что всё прошло хорошо и всё создалось 
- Заходим в C:\Program Files\PostgreSQL\13\data\pg_hba.conf и у всех методов ставим trust ( иначе падает :((( )

- Отлично, база поставилась, можно пользоваться




# Если после выполнения всех пунктов возникли проблемы при запуске приложения в IntelliJIDEA
 
 0. Устанавливаем плагин Database Tools and SQL для IntelliJIDEA (если еще не установлен)
 1. Открываем вкладку "database" на правой панели.
 2. Жмем плюсик, выбираем Data Source -> PostgreSQL
 3. Вводим все необходимые настройки: URL, пароль и.т.д. ![Настройки](https://sun9-64.userapi.com/ATB_q0YgduzZFHCNzHhpgdEQwJ1miifP4qv6DQ/9iUqS70Xkjk.jpg)
 4. Жмем TestConnection, должен быть такой результат <br/>
    ![testConnection](https://sun9-6.userapi.com/eQ1iO9Lk7Ojv3b8MZvGKLLAXVHR8jurTrCCAiQ/hyRi-0wFSLA.jpg)
 5. Жмем ОК, должно быть как-то так <br/> ![результат](https://sun9-65.userapi.com/KOf-HnkulLyu8gjRZcH4E-s2EiSQPC6b3cyZOg/w92xGlE3qWw.jpg)

Теперь проект должен запускаться нормально, в Entity-классах не должно быть ошибок 

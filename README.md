## Описание проекта:
Это простая система управления задачами, разработанная с использованием Java и Spring. 
Она позволяет создавать, редактировать, удалять и просматривать задачи, а также оставлять комментарии к ним.
Администратор может управлять всеми задачами, пользователи могут управлять своими задачами, если указаны как исполнитель.

## Технологии
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security + jwt
- PostgreSQL
- Swagger/OpenAPI
- Docker Compose

## Почта для отправки уведомлений новым пользователям
Для того чтобы пользователи могли получать сообщения на почту с кодом подтверждения при регистрации, необходимо создать учетную запись на https://myaccount.google.com › apppasswords.

После вам выдадут пароль, который необходимо использовать в переменных окружения. 

<img width="545" alt="image" src="https://github.com/user-attachments/assets/2801876b-431c-4efb-8637-05a6f863cf35" />



## Переменные окружения
Для запуска проекта необходимо прописать файл .env

SPRING_DATASOURCE_USERNAME=ИМЯ_ПОЛЬЗОВАТЕЛЯ

SPRING_DATASOURCE_PASSWORD=ПАРОЛЬ

JWT_SECRET_KEY=СЕКРЕТНЫЙ_КЛЮЧ_JWT

SUPPORT_EMAIL=ВАША_ПОЧТА

APP_PASSWORD=ПАРОЛЬ

## Как запустить проект?
1. Клонируйте репозиторий
2. Перейдите в директорию проекта: cd 'корневая папка репозитория'
3. Соберите и запустите Docker-образ для приложения: docker-compose up --build
5. После запуска контейнеров вы сможете зайти в браузер по адресу http://localhost:8080/swagger-ui/index.html#/ и воспользоваться приложением. 
База данных будет доступна на порту 5433 внутри контейнера.

## Как пользоваться приложением?

После того, как вы собрали докер-образ приложения и запустили приложение - пройдите регистрацию пользователя. 

После регистрации пользователя ему автоматически присвоится роль - USER. 

Далее вам необходимо подтвердить аккаунт с помощью одноразового кода, отправленного вам на почту.

Пройдите аутентификацию под своим учетными данными, скопируйте jwt-токен и вставьте его в authorize в swagger-ui.

<img width="175" alt="image" src="https://github.com/user-attachments/assets/f892f726-d083-4440-a2e2-9b5c7689b792" />


После чего вы получите доступ ко всем end-поинтам пользователя.

## Как воспрользоваться правами администратора? 
Для того чтобы воспользоваться end-поинтами для администратора, необходимо пройти аутентификацию для администратора.

Логин: admin@example.com

Пароль: Admin1234


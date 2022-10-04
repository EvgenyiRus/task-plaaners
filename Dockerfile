FROM openjdk:16-alpine3.13
ENV APP_HOME=/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle $APP_HOME
COPY gradle $APP_HOME/gradle
RUN gradle build || return 0
COPY . .
CMD ["gradle", "bootRun"]
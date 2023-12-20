FROM rocker/rstudio:4.2.2

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk maven
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/

RUN R CMD javareconf

RUN R -e "install.packages('rJava')"

WORKDIR /home
COPY . .

RUN mvn clean package

EXPOSE 8787

CMD ["R", "-f", "debug_sqlite.R"]
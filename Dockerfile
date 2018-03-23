FROM debian:stretch

ENV GRADLE_OPTS -Dfile.encoding=utf-8

#Dependencies installation and gradle download
RUN apt-get update && apt-get install -y wget unzip default-jdk && \
	cd /root && \
    wget https://services.gradle.org/distributions/gradle-3.5-bin.zip && \
    unzip gradle-3.5-bin.zip && \
    ln -s /root/gradle-3.5/bin/gradle /usr/local/bin/gradle && \
    rm gradle-3.5-bin.zip

WORKDIR /root

COPY ./ ./

ENTRYPOINT ["/root/gradle-3.5/bin/gradle"]
CMD ["run"]

EXPOSE 8443
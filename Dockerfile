FROM repo.gradiant.org:9006/hgda-java:1.0.0

WORKDIR /root

COPY ./ ./

ENTRYPOINT ["/root/gradle-3.5/bin/gradle"]
CMD ["run"]

EXPOSE 8443
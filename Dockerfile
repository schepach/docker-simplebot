FROM jboss/wildfly
ENV JBOSS_CLI /opt/jboss/wildfly/bin/jboss-cli.sh
ENV LC_CTYPE=en_US.UTF-8

# Folder and .db file for SQLite Datasource
RUN mkdir /opt/jboss/sqlite/
RUN touch /opt/jboss/sqlite/main.db

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin --silent

RUN /bin/sh -c '/opt/jboss/wildfly/bin/standalone.sh -b 127.0.0.1 -bmanagement 0.0.0.0 &' && \
      sleep 10 && \
      # Download sqlite driver
      curl --location --output /opt/jboss/sqlite-jdbc-3.32.3.2.jar --url https://github.com/xerial/sqlite-jdbc/releases/download/3.32.3.2/sqlite-jdbc-3.32.3.2.jar && \
      # Add driver and create datasource
      $JBOSS_CLI --command="module add --name=org.sqlite --resources=sqlite-jdbc-3.32.3.2.jar --dependencies=javax.api,javax.transaction.api" && \
      $JBOSS_CLI -c --controller=http-remoting://localhost:9990 --command="/subsystem=datasources/jdbc-driver=sqlite:add(driver-name=sqlite,driver-module-name=org.sqlite,driver-class-name=org.sqlite.JDBC)" && \
      $JBOSS_CLI -c --controller=http-remoting://localhost:9990 --command="data-source add --jndi-name=java:jboss/datasources/SqliteDS --name=SqliteDS --connection-url=jdbc:sqlite:/opt/jboss/sqlite/main.db --driver-name=sqlite" && \
      $JBOSS_CLI --connect --command=:shutdown && \
      rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current/* && \
      rm -f /opt/jboss/*.jar

ADD /docker-simplebot-ear/target/*.ear /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "127.0.0.1","-bmanagement","0.0.0.0"]
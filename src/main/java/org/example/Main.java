package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.dispatch.*;
import akka.persistence.typed.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import akka.dispatch.UnboundedMailbox;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.example.filterFileActor.FilterFileActor;
import org.example.getFileActor.GetFileActor;
import org.example.putFileActor.PutFileActor;

public class Main {
    public static void main(String[] args) {

        // create the persistence IDs for the actors
        PersistenceId getFileActorPersistenceId = PersistenceId.ofUniqueId("getFileActor");
        PersistenceId filterFileActorPersistenceId = PersistenceId.ofUniqueId("filterFileActor");
        PersistenceId putFileActorPersistenceId = PersistenceId.ofUniqueId("putFileActor");


// load the normal config stack (system props, then application.conf, then reference.conf)
        Config regularConfig = ConfigFactory.load();
        Config file = ConfigFactory.load("config.conf");
        Config combined = file.withFallback(regularConfig);
        Config complete = ConfigFactory.load(combined);

//        System.out.println(complete.root().render());
//        System.out.println(complete.getString("default-mailbox.mailbox-type"));

        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(),"FileProcessingSystem",complete);


        // create the actors using EventSourcedBehavior
        ActorRef<String> putFileActorRef = system
                .systemActorOf(
                        PutFileActor.create(putFileActorPersistenceId, "/Users/smavani/INPUT_OUTPUT_FOR_TESTING/OUTPUT/output"),
                        "putFileActor"
                ,MailboxSelector.bounded(1000));            //Default Mailbox-type = SingleConsumerOnlyUnboundedMailbox,So we can change it to bounded mailbox-type

        ActorRef<String> filterFileActorRef = system
                .systemActorOf(
                        FilterFileActor.create(putFileActorRef,filterFileActorPersistenceId),
                        "filterFileActor"
                ,Props.empty());

        ActorRef<String> getFileActorRef = system
                .systemActorOf(
                        GetFileActor.create(filterFileActorRef,getFileActorPersistenceId),
                        "getFileActor"
                ,Props.empty());

        // tell the getFileActor to start processing files
        getFileActorRef.tell(("/Users/smavani/INPUT_OUTPUT_FOR_TESTING/INPUT"));

    }
}
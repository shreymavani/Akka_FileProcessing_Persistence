package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.dispatch.*;
import akka.persistence.typed.*;
//import akka.dispatch.BoundedMessageQueueWithOverflow;
import java.util.List;
import java.util.concurrent.TimeUnit;
import akka.dispatch.UnboundedMailbox;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.typesafe.config.ConfigFactory;
import org.example.filterFileActor.FilterFileActor;
import org.example.getFileActor.GetFileActor;
import org.example.putFileActor.PutFileActor;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class Main {
    public static void main(String[] args) {
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(),"FileProcessingSystem",ConfigFactory.load());

        // create the persistence IDs for the actors
        PersistenceId getFileActorPersistenceId = PersistenceId.ofUniqueId("getFileActor");
        PersistenceId filterFileActorPersistenceId = PersistenceId.ofUniqueId("filterFileActor");
        PersistenceId putFileActorPersistenceId = PersistenceId.ofUniqueId("putFileActor");


//        MailboxType myMailboxType = Mailboxes.get().lookupByQueueType(UnboundedMailbox.class);


        // create the actors using EventSourcedBehavior
        ActorRef<String> putFileActorRef = system
                .systemActorOf(
                        PutFileActor.create(putFileActorPersistenceId, "/Users/smavani/INPUT_OUTPUT_FOR_TESTING/OUTPUT/output"),
                        "putFileActor"
                ,Props.empty());

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
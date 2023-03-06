package org.example.getFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetFileActor extends EventSourcedBehavior<String, String, List<String>> {

    private final ActorRef<String> filterActor;

    private GetFileActor(ActorContext<String> context, ActorRef<String> filterActor, PersistenceId persistenceId) {
        super(persistenceId);
        this.filterActor = filterActor;
    }

    public static Behavior<String> create(ActorRef<String> filterActor, PersistenceId persistenceId) {
        return Behaviors.setup(context -> new GetFileActor(context, filterActor, persistenceId));
    }

    @Override
    public List<String> emptyState() {
        return new ArrayList<String>();
    }

    @Override
    public CommandHandler<String, String, List<String>> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::onHandleDirectory)
                .build();
    }

    private Effect<String, List<String>> onHandleDirectory(List<String> state, String cmd) {
        File directory = new File(cmd);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String filePath = file.getAbsolutePath();
                    // send file path to filter actor
                    filterActor.tell(filePath);
                }
            }
        }
        return Effect().none();
    }


    @Override
    public EventHandler<List<String>, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .build();
    }

}

package org.example.getFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetFileActor extends EventSourcedBehavior<String, String, GetFileActor.State> {

    private final ActorRef<String> filterActor;


    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }
    private GetFileActor(ActorContext<String> context, ActorRef<String> filterActor, PersistenceId persistenceId) {
        super(persistenceId);
        this.filterActor = filterActor;
    }

    public static Behavior<String> create(ActorRef<String> filterActor, PersistenceId persistenceId) {
        return Behaviors.setup(context -> new GetFileActor(context, filterActor, persistenceId));
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<String, String, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::onHandleDirectory)
                .build();
    }

    private Effect<String, State> onHandleDirectory(State state, String path) {
        File directory = new File(path);
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
    public EventHandler<State, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .build();
    }
}

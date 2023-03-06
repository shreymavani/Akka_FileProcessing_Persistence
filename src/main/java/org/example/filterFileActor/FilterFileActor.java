package org.example.filterFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import akka.persistence.typed.javadsl.Recovery;
import org.example.getFileActor.GetFileActor;

import java.util.*;

public class FilterFileActor extends EventSourcedBehavior<String, String, FilterFileActor.State> {

    private final ActorRef<String> putFileActorRef;

    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public FilterFileActor.State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }
    public FilterFileActor(ActorContext<String> context, ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        super(persistenceId);
        this.putFileActorRef = putFileActorRef;
    }

    public static Behavior<String> create(ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        return Behaviors.setup(context -> new FilterFileActor(context, putFileActorRef,persistenceId));
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<String, String, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::filterFile)
                .build();
    }

    private Effect<String, State> filterFile(State state, String file) {
        if (file.endsWith(".txt")) {
            state.addItem(file);
            putFileActorRef.tell(file);
        }
        return Effect().persist(file).thenRun(() -> {
            // Log successful persist event
//            getContext().getLog().info("File {} persisted successfully", file);
        });
    }

    @Override
    public EventHandler<State, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(String.class, (state, event) -> {
                    state.addItem(event);
                    return state;
                })
                .build();
    }

//    public Recovery recovery() {
//        return Recovery.withSnapshotSelectionCriteria(SnapshotSelectionCriteria.none());
//    }
}


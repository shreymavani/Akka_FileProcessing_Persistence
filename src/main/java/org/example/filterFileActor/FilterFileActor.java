package org.example.filterFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import akka.persistence.typed.javadsl.Recovery;
import java.util.*;

public class FilterFileActor extends EventSourcedBehavior<String, String, List<String>> {

    private final ActorRef<String> putFileActorRef;

    public FilterFileActor(ActorContext<String> context, ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        super(persistenceId);
        this.putFileActorRef = putFileActorRef;
    }

    public static Behavior<String> create(ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        return Behaviors.setup(context -> new FilterFileActor(context, putFileActorRef,persistenceId));
    }

    @Override
    public List<String> emptyState() {
        return new ArrayList<String>();
    }

    @Override
    public CommandHandler<String, String, List<String>> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::filterFile)
                .build();
    }

    private Effect<String, List<String>> filterFile(List<String> state, String file) {
        if (file.endsWith(".txt")) {
            state.add(file);
            putFileActorRef.tell(file);
        }
        return Effect().persist(file).thenRun(() -> {
            // Log successful persist event
//            getContext().getLog().info("File {} persisted successfully", file);
        });
    }

    @Override
    public EventHandler<List<String>, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(String.class, (state, event) -> {
                    state.add(event);
                    return state;
                })
                .build();
    }

//    public Recovery recovery() {
//        return Recovery.withSnapshotSelectionCriteria(SnapshotSelectionCriteria.none());
//    }
}


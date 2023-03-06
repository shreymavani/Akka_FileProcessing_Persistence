package org.example.putFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import org.example.getFileActor.GetFileActor;

import java.util.*;

//import static java.awt.TexturePaintContext.getContext;

public class PutFileActor extends EventSourcedBehavior<String, String, PutFileActor.State> {

    private final String outputDir;

    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public PutFileActor.State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }
    public PutFileActor(ActorContext<String> context, String outputDir,PersistenceId persistenceId) {
        super(persistenceId);
        this.outputDir = outputDir;
    }

    public static Behavior<String> create(PersistenceId persistenceId,String outputDir) {
        return Behaviors.setup(context -> new PutFileActor(context, outputDir,persistenceId));
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<String, String , State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::putFile)
                .build();
    }

    private Effect<String, State> putFile(State state, String file) {
        state.addItem(file);
        // Put the file in the output directory
        // ...
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

//    @Override
//    public Recovery recovery() {
//        return Recovery.withSnapshotSelectionCriteria(SnapshotSelectionCriteria.none());
//    }
}


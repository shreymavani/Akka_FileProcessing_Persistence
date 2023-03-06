package org.example.filterFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import akka.persistence.typed.javadsl.Recovery;
import org.example.getFileActor.GetFileActor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Effect<String, State> filterFile(State state, String data) {
        String filterData = stringFiltering(data,"Shrey");
        putFileActorRef.tell(filterData);
        return Effect().persist(data).thenRun(() -> {
            // Log successful persist event
//            getContext().getLog().info("File {} persisted successfully", file);
        });
    }

    public String stringFiltering(String inputData, String regex) {
        String[] splitData = inputData.split("\n");
        String filterData = "";
//        System.out.println(splitData.length);
        for (String line : splitData) {

            Pattern pattern = Pattern.compile(regex);            // Compile the pattern

            Matcher matcher = pattern.matcher(line);             // Replace the password with the string ""

            filterData += (matcher.replaceAll("Kuldeep") + "\n");     // Replace the word "password" and password details with the string ""
        }

        return filterData;

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


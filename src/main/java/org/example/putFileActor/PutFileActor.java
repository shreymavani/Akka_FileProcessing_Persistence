package org.example.putFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//import static java.awt.TexturePaintContext.getContext;

public class PutFileActor extends EventSourcedBehavior<String, String, List<String>> {

    private final String outputDir;

    public PutFileActor(String outputDir,PersistenceId persistenceId) {
        super(persistenceId);
        this.outputDir = outputDir;
    }

    public static Behavior<String> create(PersistenceId persistenceId,String outputDir) {
        return Behaviors.setup(context -> new PutFileActor(outputDir,persistenceId));
    }

    @Override
    public List<String> emptyState() {
        return new ArrayList<>();
    }

    @Override
    public CommandHandler<String, String , List<String>> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::putFile)
                .build();
    }

    private Effect<String, List<String>> putFile(List<String> state, String data){

        FileWriter fw = null;
        try {
            fw = new FileWriter(outputDir, true);
            fw.write(data);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Effect().persist(data).thenRun(() -> {
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

//    @Override
//    public Recovery recovery() {
//        return Recovery.withSnapshotSelectionCriteria(SnapshotSelectionCriteria.none());
//    }
}


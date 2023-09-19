package machine.builder;

import jaxb.generated.*;
import java.util.*;
import object.numbering.RomanNumber;

import machine.MachineImpl;
import machine.components.storage.MachineStorage;
import machine.components.keyboard.Keyboard;
import machine.components.translators.reflector.Reflector;
import machine.components.translators.reflector.ReflectorImpl;
import machine.components.rotor.Direction;
import machine.components.rotor.Rotor;
import machine.components.rotor.RotorImpl;

public class MachineBuilder {
    public MachineImpl buildEnigmaMachine(CTEEnigma cteEnigma) {
        Keyboard keyboard = buildKeyboardFromCTEEnigma(cteEnigma);
        MachineStorage machineStorage = buildStorageFromCTEEnigma(cteEnigma, keyboard);

        return new MachineImpl(keyboard, machineStorage, cteEnigma.getCTEMachine().getRotorsCount());
    }

    private Keyboard buildKeyboardFromCTEEnigma(CTEEnigma cteEnigma) {
        String abc = cteEnigma.getCTEMachine().getABC().trim().toUpperCase();
        Map<Character, Integer> keyToIndex = new HashMap<>();
        int keyCount = 0;

        for (Character currKey : abc.toCharArray()) {
                keyToIndex.put(currKey, keyCount++);
        }

        return new Keyboard(keyToIndex);
    }

    private MachineStorage buildStorageFromCTEEnigma(CTEEnigma cteEnigma, Keyboard keyboard) {
        List<CTERotor> allCTERotors= cteEnigma.getCTEMachine().getCTERotors().getCTERotor();
        List<CTEReflector> allCTEReflectors = cteEnigma.getCTEMachine().getCTEReflectors().getCTEReflector();
        List<Rotor> allRotorsTemp = new ArrayList<>();
        Map<RomanNumber, Reflector> romanNumberToReflector= new HashMap<>();

        allCTERotors.forEach(x -> allRotorsTemp.add(buildRotorFromCTERotor(x, keyboard)));

        List<Rotor> allRotors = new ArrayList<>(Collections.nCopies(allRotorsTemp.size(), null));

        allRotorsTemp.forEach(x -> allRotors.set(x.getRotorID() - 1, x));

        allCTEReflectors.forEach(x -> romanNumberToReflector.put(RomanNumber.fromString(x.getId()), buildReflectorFromCTEReflector(x, keyboard)));

        return new MachineStorage(allRotors, romanNumberToReflector);
    }

    private Reflector buildReflectorFromCTEReflector(CTEReflector currReflector, Keyboard keyboard) {
        List<Integer> indexTranslationList = new ArrayList<>(Collections.nCopies(keyboard.getKeyCount(), 0));

        currReflector.getCTEReflect().forEach(x -> {
            indexTranslationList.set(x.getInput() - 1, x.getOutput() - 1);
            indexTranslationList.set(x.getOutput() - 1, x.getInput() - 1);
        });

        return new ReflectorImpl(RomanNumber.fromString(currReflector.getId()), indexTranslationList);
    }

    private Rotor buildRotorFromCTERotor(CTERotor cteRotor, Keyboard keyboard) {
        Map<Direction, List<Integer>> directionToIndexTranslationList = new HashMap<>();
        List<Integer> forwardsIndexTranslationList = new ArrayList<>(Collections.nCopies(keyboard.getKeyCount(), 0));
        List<Integer> backwardsIndexTranslationList = new ArrayList<>(Collections.nCopies(keyboard.getKeyCount(), 0));
        Map<Integer, Integer> keyIndexToRightSidePosition = new HashMap<>();
        Map<Integer, Integer> rightSidePositionToKeyIndex = new HashMap<>();
        int currPosInTranslationList = 0;

        for (CTEPositioning currPositioningForRightSide : cteRotor.getCTEPositioning()) {
            for (int i = 0; i < cteRotor.getCTEPositioning().size(); i++) {
                if (cteRotor.getCTEPositioning().get(i).getLeft().equals(currPositioningForRightSide.getRight().toUpperCase())) {
                    forwardsIndexTranslationList.set(currPosInTranslationList, i);
                }
            }

            keyIndexToRightSidePosition.put(keyboard.getIndexForKey(currPositioningForRightSide.getRight().toUpperCase().charAt(0)), currPosInTranslationList);
            currPosInTranslationList++;
        }

        for (int i = 0; i < forwardsIndexTranslationList.size(); i++) {
            backwardsIndexTranslationList.set(forwardsIndexTranslationList.get(i), i);
        }

        keyIndexToRightSidePosition.forEach((keyIndex, rightSidePosition) -> rightSidePositionToKeyIndex.put(rightSidePosition, keyIndex));

        directionToIndexTranslationList.put(Direction.Forwards, forwardsIndexTranslationList);
        directionToIndexTranslationList.put(Direction.Backwards, backwardsIndexTranslationList);

        return new RotorImpl(cteRotor.getId(), cteRotor.getNotch() - 1, directionToIndexTranslationList, keyIndexToRightSidePosition, rightSidePositionToKeyIndex, keyboard.getKeyCount());
    }
}
package org.example.Managers;

import org.example.CollectionModel.HumanBeing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.LogManager;

/**
 * Класс менеджера коллекции, хранящий ее саму и информацию о ней
 */
public class CollectionManager {
    private final Stack<HumanBeing> collection = new Stack<>();
    private LocalDateTime lastInitTime;
    private final LocalDateTime InitTime;
    private LocalDateTime lastSaveTime;
    private float maxPrice = 0;

    static final Logger collectionManagerLogger = LoggerFactory.getLogger(CollectionManager.class);

    private static long idPoint = 0L;

    public CollectionManager() {
        this.InitTime = LocalDateTime.now();
        this.lastSaveTime = null;
    }

    public static long incIdPoint() {
        return ++idPoint;
    }

    public static void updateIdPoint(Stack<HumanBeing> collection) {
        idPoint = collection.stream()
                .filter(Objects::nonNull)
                .map(HumanBeing::getId)
                .mapToLong(Long::longValue)
                .max().orElse(0);
        collectionManagerLogger.info("Обновлен idPoint на " + idPoint);
    }

    public static String formatTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        if (localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                .equals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))) {
            return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        return localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public static String formatTime(Date date) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return formatTime(localDateTime);
    }

    public Stack<HumanBeing> getCollection() {
        return collection;
    }

    public String getLastInitTime() {
        return formatTime(lastInitTime);
    }

    public String getLastSaveTime() {
        return formatTime(lastSaveTime);
    }

    public String getCollectionType() {
        return collection.getClass().getName();
    }

    public int getSize() {
        return collection.size();
    }

    public void clear() {
        collection.clear();
        lastInitTime = LocalDateTime.now();
        collectionManagerLogger.info("Коллекция очищена");
    }

    public HumanBeing getLast() {
        return collection.peek();
    }

    public HumanBeing getById(int id) {
        for (HumanBeing element : collection) {
            if (element.getId() == id) return element;
        }
        return null;
    }

    public void addElement(HumanBeing humanBeing) {
        this.lastSaveTime = LocalDateTime.now();
        collection.push(humanBeing);
        float newPrice = humanBeing.getPrice();
        if (newPrice > maxPrice) maxPrice = newPrice;
        collectionManagerLogger.info("Добавлен новый элемент в коллекцию");
    }

    public void addElement(Collection<HumanBeing> collection) {
        if (collection == null) return;
        for (HumanBeing humanBeing : collection) {
            addElement(humanBeing);
        }
    }

    public void removeLast() throws EmptyStackException {
        if (collection.isEmpty()) throw new EmptyStackException();
        collection.pop();
        this.lastSaveTime = LocalDateTime.now();
    }

    public void removeElement(HumanBeing humanBeing) {
        collection.remove(humanBeing);
        this.lastSaveTime = LocalDateTime.now();
    }

    public void removeElements(Collection<HumanBeing> collection) {
        this.collection.removeAll(collection);
    }

    public boolean checkExistById(long id) {
        return collection.stream().anyMatch(o -> o.getId() == id);
    }

    public HumanBeing getById(long id) {
        for (HumanBeing humanBeing : collection) {
            if (humanBeing.getId() == id) return humanBeing;
        }
        return null;
    }

    public void editById(long id, HumanBeing newHumanBeing) {
        HumanBeing oldHumanBeing = getById(id);
        removeElement(oldHumanBeing);
        newHumanBeing.setId(id);
        addElement(newHumanBeing);
        this.lastSaveTime = LocalDateTime.now();
        collectionManagerLogger.info("Изменен объект с id=" + id + ": " + newHumanBeing);
    }

    public LocalDateTime getInitTimeInDate() {
        return InitTime;
    }

    public LocalDateTime getLastSaveTimeInDate() {
        return lastSaveTime;
    }

    public void setLastInitTime(LocalDateTime lastInitTime) {
        this.lastInitTime = lastInitTime;
    }

    public void setLastSaveTime(LocalDateTime lastSaveTime) {
        this.lastSaveTime = lastSaveTime;
    }

    public float getMaxPrice() {
        return maxPrice;
    }

    @Override
    public String toString() {
        if (collection.isEmpty()) return "Коллекция пуста";
        Long last = getLast().getId();

        StringBuilder collectionInfo = new StringBuilder();
        for (HumanBeing humanBeing : collection) {
            collectionInfo.append(humanBeing);
            if (!humanBeing.getId().equals(last)) collectionInfo.append("\n");
        }
        return collectionInfo.toString();
    }
}

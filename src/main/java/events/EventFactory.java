
public class EventFactory {

    public static Event createEvent(String eventName) {
        switch (eventName) {
            case "feud":
                return new feudEvent();
            case "fraternal feuds":
                return new fraternalfeudsEvent();
            case "invention":
                return new inventionEvent();
            case "trade ships race":
                return new tradeShipRaceEvent();
            case "traveling merchant":
                return new travelingMerchantEvent();
            case "year of plenty":
                return new yearOfThePlentyEvent();
            case "yule":
                return new yuleEvent();
            default:
                return new defaultEvent();
        }
    }

}

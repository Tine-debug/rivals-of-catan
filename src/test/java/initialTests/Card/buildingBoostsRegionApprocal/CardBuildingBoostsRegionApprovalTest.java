import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import Card.*;
import Player.*;
import Turns.*;
import Points.*;

import org.approvaltests.Approvals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CardBuildingBoostsRegionApprovalTest{

    static String[] buildingNames = {"Iron Foundry", "Grain Mill", "Lumber Camp",  "Brick Factory", "Weaver’s Shop", "Weaver's Shop", "x", null};
    static String[] regionNames = {"Mountain","Field", "Forest", "Hill", "Pasture", "x", null};


    static Stream<Arguments> RegionandBuldingNamesProvider() {
    List<Arguments> args = new ArrayList<>();
    for (int i = 0; i < buildingNames.length ; i++) {
        for (int j = 0; j < regionNames.length; j++)
            args.add(Arguments.of( buildingNames[i], regionNames[j]));    
    }
    return args.stream();
    }


        @ParameterizedTest(name = "[{index}]-{0},{1}buildingBoostsRegion")
        @MethodSource("RegionandBuldingNamesProvider")
        void testCardbuildingBoostsRegion (String buildingname, String regionname) {
            String result = String.valueOf(Turns.buildingBoostsRegion(buildingname, regionname));
            Approvals.verify(result, Approvals.NAMES.withParameters(buildingname, regionname));
        }
}
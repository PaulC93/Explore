package com.paul.explore

import com.paul.explore.model.Map
import spock.lang.Specification

class MapTest extends Specification {


    def 'should update'() {
        expect:
        3 == Map.updateMinIfNecessary(5, 3);
        3 == Map.updateMinIfNecessary(3, 4);
        8 == Map.updateMaxIfNecessary(5, 8);
        8 == Map.updateMaxIfNecessary(8, 5);
    }

}

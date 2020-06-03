package shi.quan.rcpsp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.vo.Duo;

import java.util.*;
import java.util.stream.Collectors;

public class RangeUtil {
   private static final Logger logger = LoggerFactory.getLogger(RangeUtil.class);

   public interface AmountCalculator<AmountType> {
      AmountType zero();
      AmountType plus(AmountType a, AmountType b);
      AmountType minus(AmountType a, AmountType b);

   }

   public interface ResourceAmountProvider<TimeType, AmountType> {
      AmountType getResourceByTimeRange(TimeType start, TimeType end);
   }

   public static
   <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>>
   boolean resourceCalculationByTimeRange(
           List<Duo<TimeType, TimeType>> ranges
           , Map<Duo<TimeType,TimeType>, AmountType> resourceMap
           , Duo<TimeType, TimeType> selected
           , AmountCalculator<AmountType> amountCalculator
           , ResourceAmountProvider<TimeType, AmountType> resourceAmountProvider) {
      logger.info("[resourceCalculationByTimeRange]");

      boolean ret = true;

      List<TimeType> splitters = getRangeSplitter(ranges, selected).stream().sorted().collect(Collectors.toList());

      for(int i = 1; i < splitters.size(); ++i) {
         Duo<TimeType, TimeType> r = Duo.duo(splitters.get(i - 1), splitters.get(i));

         AmountType amount = amountCalculator.zero();

         for (Duo<TimeType, TimeType> range : ranges) {
            if (!isEmptyRange(intersect(r, range))) {
               AmountType resourceOfRange = resourceMap.get(range);
               amount = amountCalculator.plus(amount, resourceOfRange);
            }
         }

         AmountType resourceThreshold = resourceAmountProvider.getResourceByTimeRange(r.getK(), r.getV());

         logger.info("r : {}, amount : {}, resource : {}", r, amount, resourceThreshold);

         ret = resourceThreshold.compareTo(amount) >= 0;

         logger.info("ret : {}", ret);

         if (!ret) {
            break;
         }
      }

      return ret;
   }

   public static
   <TimeType extends Comparable<TimeType>>
   Set<TimeType> getRangeSplitter(List<Duo<TimeType, TimeType>> ranges, Duo<TimeType, TimeType> selected) {
      logger.info("[getRangeSplitter] ranges : {}, selected : {}", ranges, selected);
      Set<TimeType> splitters = new HashSet<>();

      splitters.add(selected.getK());
      splitters.add(selected.getV());

      List<Duo<TimeType, TimeType>> involved = new ArrayList<>();

      for(Duo<TimeType, TimeType> range : ranges) {
         if(range != selected) {
            Duo<TimeType, TimeType> intersect = intersect(range, selected);

            if(!isEmptyRange(intersect)) {
               splitters.add(intersect.getK());
               splitters.add(intersect.getV());

               involved.add(range);
            }
         }
      }

      logger.info("selected : {}", selected);
      logger.info("Involved : {}", involved);
      logger.info("splitters : {}", splitters);

      return splitters;
   }

   private static
   <TimeType extends Comparable<TimeType>>
   Duo<TimeType,TimeType> intersect(Duo<TimeType,TimeType> range, Duo<TimeType,TimeType> selected) {
      return Duo.duo(max(range.getK(), selected.getK()), min(range.getV(), selected.getV()));
   }

   private static
   <TimeType extends Comparable<TimeType>>
   TimeType min(TimeType v1, TimeType v2) {
      return v1 != null ? (v1.compareTo(v2) <= 0 ? v1 : v2) : v2;
   }

   private static
   <TimeType extends Comparable<TimeType>>
   TimeType max(TimeType v1, TimeType v2) {
      return v1 != null && v2 != null ? (v1.compareTo(v2) >= 0 ? v1 : v2) : v2;
   }

   private static
   <TimeType extends Comparable<TimeType>>
   boolean isEmptyRange(Duo<TimeType, TimeType> inteorsect) {
      return inteorsect.getK() != null && inteorsect.getV() != null && (inteorsect.getK().compareTo(inteorsect.getV()) >= 0);
   }
}

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
      /**
       * Calculate the minimum resource limitation in range [start, end].
       */
      AmountType getResourceByTimeRange(TimeType start, TimeType end);

      /**
       * Calculate the extra time for the resource.
       * e.g. Break time for a human resource, or maintenance time for a machine, etc.
       */
      TimeType getResourceExtraTime(TimeType start, TimeType end);
   }

   public static
   <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>>
   boolean resourceCalculationByTimeRange(boolean verbose,
           List<Duo<TimeType, TimeType>> ranges
           , Map<Duo<TimeType,TimeType>, AmountType> resourceMap
           , Duo<TimeType, TimeType> selected
           , AmountCalculator<AmountType> amountCalculator
           , ResourceAmountProvider<TimeType, AmountType> resourceAmountProvider) {

      if(verbose) logger.info("[resourceCalculationByTimeRange]");
//      if(verbose) logger.info("\tranges : {}", ranges);
//      if(verbose) logger.info("\tresourceMap : {}", resourceMap);
//      if(verbose) logger.info("\tselected : {}", selected);

      boolean ret = true;

      List<TimeType> splitters = getRangeSplitter(verbose, ranges, selected).stream().sorted().collect(Collectors.toList());

      if(verbose) logger.info("splitters : {}", splitters);

      for(int i = 1; i < splitters.size(); ++i) {
         Duo<TimeType, TimeType> r = Duo.duo(splitters.get(i - 1), splitters.get(i));

         AmountType amount = amountCalculator.zero();

         Map<Duo<TimeType,TimeType>, AmountType> selectedMap = new HashMap<>();

         for (Duo<TimeType, TimeType> range : ranges) {
            if (!isEmptyRange(intersect(r, range))) {
               AmountType resourceOfRange = resourceMap.get(range);
               amount = amountCalculator.plus(amount, resourceOfRange);
               selectedMap.put(range, resourceOfRange);
            }
         }

         AmountType resourceThreshold = resourceAmountProvider.getResourceByTimeRange(r.getK(), r.getV());

         if(verbose) logger.info("r : {}, amount : {}, resource : {}", r, amount, resourceThreshold);
         if(verbose) logger.info("selectedMap : {}", selectedMap);

         ret = resourceThreshold.compareTo(amount) >= 0;

//         if(verbose) logger.info("ret : {}", ret);

         if (!ret) {
            break;
         }
      }

      if(verbose) logger.info("ret : {}", ret);

      return ret;
   }

   public static
   <TimeType extends Comparable<TimeType>>
   Set<TimeType> getRangeSplitter(boolean verbose, List<Duo<TimeType, TimeType>> ranges, Duo<TimeType, TimeType> selected) {
//      if(verbose) logger.info("[getRangeSplitter] ranges : {}, selected : {}", ranges, selected);
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

//      if(verbose) logger.info("selected : {}", selected);
//      if(verbose) logger.info("Involved : {}", involved);
//      if(verbose) logger.info("splitters : {}", splitters);

      return splitters;
   }

   public static
   <TimeType extends Comparable<TimeType>>
   Duo<TimeType,TimeType> intersect(Duo<TimeType,TimeType> range, Duo<TimeType,TimeType> selected) {
      return Duo.duo(max(range.getK(), selected.getK()), min(range.getV(), selected.getV()));
   }

   public static
   <TimeType extends Comparable<TimeType>>
   TimeType min(TimeType v1, TimeType v2) {
      return v1 != null ? (v1.compareTo(v2) <= 0 ? v1 : v2) : v2;
   }

   public static
   <TimeType extends Comparable<TimeType>>
   TimeType max(TimeType v1, TimeType v2) {
      return v1 != null && v2 != null ? (v1.compareTo(v2) >= 0 ? v1 : v2) : v2;
   }

   public static
   <TimeType extends Comparable<TimeType>>
   boolean isEmptyRange(Duo<TimeType, TimeType> inteorsect) {
      return inteorsect.getK() != null && inteorsect.getV() != null && (inteorsect.getK().compareTo(inteorsect.getV()) >= 0);
   }
}

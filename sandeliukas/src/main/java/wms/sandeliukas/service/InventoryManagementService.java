package wms.sandeliukas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.InventoryProductData;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.model.RackData;
import wms.sandeliukas.repositories.ProductRepository;
import org.springframework.ui.Model;
import wms.sandeliukas.service.InventoryManagementService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InventoryManagementService {

    private static final String SHELF_TABLE = "Shelf";

    private final ProductRepository productRepository;
    private final JdbcTemplate jdbcTemplate;

    public InventoryManagementService(ProductRepository productRepository, JdbcTemplate jdbcTemplate) {
        this.productRepository = productRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InventoryProductData> itemLayoutEditRequest() {
        List<InventoryProductData> items = itemListRequest();
        List<RackData> racks = rackListRequest();
        availableSpaceInRacksCalculation(racks, items);


        
        List<InventoryProductData> prioritizedItems = itemLayoutPriorityCalculation(items);
        preliminaryItemLayoutListCalculation(prioritizedItems, racks);
        preliminaryItemLayoutListCheck(prioritizedItems);
        assignItemsToRacksAutomatically(prioritizedItems, racks);
        updateLayout();
        return itemLayoutInRacksWindow(items);
    }

    public InventoryProductData itemVolumeRequest(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prekė nerasta"));

        InventoryProductData productData = new InventoryProductData();
        productData.setId(product.getId());
        productData.setName(product.getName());
        productData.setStock(product.getInitialStock());
        productData.setVolume(readProductNumber(product.getId(), "volume", "itemVolume", "turis", "tūris").orElse(1.0));
        productData.setType(readProductText(product.getId(), "type", "kind", "category", "rusis", "rūšis").orElse("Bendra"));
        productData.setRack(readProductText(product.getId(), "rack", "shelf", "fk_Shelf", "fk_Rack", "stelažas", "stelazas").orElse("-"));
        return productData;
    }

    public boolean receivedDataCheck(InventoryProductData productData) {
        return productData != null && productData.getId() != null && productData.getVolume() != null && productData.getType() != null;
    }

    public void saveData(Model model, InventoryProductData productData) {
        model.addAttribute("productData", productData);
        model.addAttribute("items", itemLayoutEditRequest());
        model.addAttribute("products", selectItemRequest());
    }

    public List<RackData> rackDataRequest() {
        if (!tableExists(SHELF_TABLE)) {
            return List.of();
        }

        List<String> columns = getColumns(SHELF_TABLE);
        String idColumn = findColumn(columns, "id");
        String zoneColumn = findColumn(columns, "zone", "zona");
        String numberColumn = findColumn(columns, "number", "numeris");
        String volumeColumn = findColumn(columns, "maxVolume", "max_turis", "maxTūris", "capacity", "talpa");
        String typeColumn = findColumn(columns, "allowedType", "leistinaRusis", "leistinaRūšis", "type", "rusis", "rūšis");
        String statusColumn = findColumn(columns, "status", "busena", "būsena");

        if (idColumn == null) {
            return List.of();
        }

        return jdbcTemplate.query("select * from " + SHELF_TABLE, (rs, rowNum) -> {
            RackData rackData = new RackData();
            rackData.setId(rs.getInt(idColumn));
            String zone = zoneColumn == null ? "" : String.valueOf(rs.getObject(zoneColumn));
            String number = numberColumn == null ? String.valueOf(rackData.getId()) : String.valueOf(rs.getObject(numberColumn));
            rackData.setName(zone.isBlank() ? "Stelažas " + number : "Zona " + zone + ", stelažas " + number);
            rackData.setMaxVolume(volumeColumn == null ? null : readDouble(rs.getObject(volumeColumn)));
            rackData.setAllowedType(typeColumn == null ? null : readText(rs.getObject(typeColumn)));
            rackData.setStatus(statusColumn == null ? null : readText(rs.getObject(statusColumn)));
            return rackData;
        });
    }

    public boolean checkRackSuitability(RackData rackData, InventoryProductData productData) {
        if (rackData == null || productData == null) {
            return false;
        }

        boolean typeMatches = rackData.getAllowedType() == null
                || rackData.getAllowedType().isBlank()
                || rackData.getAllowedType().equalsIgnoreCase(productData.getType());
        boolean volumeMatches = rackData.getMaxVolume() == null
                || productData.getVolume() == null
                || rackData.getMaxVolume() >= productData.getVolume();

        return typeMatches && volumeMatches;
    }

    public RackData chooseCorrectRack(InventoryProductData productData) {
        return rackDataRequest().stream()
                .filter(rackData -> checkRackSuitability(rackData, productData))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tinkamas stelažas nerastas"));
    }

    public void assignmentItemToRack(InventoryProductData productData, RackData rackData) {
        if (!checkRackSuitability(rackData, productData)) {
            throw new RuntimeException("Pasirinktas stelažas netinka prekei");
        }
        productData.setRack(rackData.getName());
    }

    public void dataRackSave(RackData rackData) {
        if (rackData == null || rackData.getId() == null) {
            throw new RuntimeException("Stelažo duomenys neteisingi");
        }
    }

    public void dataItemUpdate(InventoryProductData productData) {
        if (!receivedDataCheck(productData)) {
            throw new RuntimeException("Prekės duomenys neteisingi");
        }
    }

    public List<LocalDate> itemDepartureDatesRequest() {
        return List.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));
    }

    public List<LocalDate> sortDates(List<LocalDate> departureDates) {
        return departureDates.stream().sorted().toList();
    }

    public List<InventoryProductData> prioritizeByVolumeAndType(List<InventoryProductData> items) {
        return items.stream()
                .sorted(Comparator.comparing(InventoryProductData::getType)
                        .thenComparing(InventoryProductData::getVolume, Comparator.reverseOrder()))
                .toList();
    }

    public List<InventoryProductData> updateLayout() {
        List<LocalDate> departureDates = itemDepartureDatesRequest();
        sortDates(departureDates);
        List<InventoryProductData> prioritizedItems = prioritizeByVolumeAndType(itemListRequest());
        return prioritizedItems;
    }

    public List<InventoryProductData> itemListRequest() {
        return productRepository.findAll().stream()
                .map(product -> itemVolumeRequest(product.getId()))
                .toList();
    }

    public List<RackData> rackListRequest() {
        return rackDataRequest();
    }

    public List<RackData> availableSpaceInRacksCalculation(List<RackData> racks, List<InventoryProductData> items) {
        return racks;
    }

    public List<InventoryProductData> itemLayoutPriorityCalculation(List<InventoryProductData> items) {
        return prioritizeByVolumeAndType(items);
    }

    public List<InventoryProductData> preliminaryItemLayoutListCalculation(List<InventoryProductData> items, List<RackData> racks) {
        return new ArrayList<>(items);
    }

    public boolean preliminaryItemLayoutListCheck(List<InventoryProductData> items) {
        return items != null;
    }

    public List<InventoryProductData> itemLayoutInRacksWindow(List<InventoryProductData> items) {
        return items;
    }

    public void assignItemsToRacksAutomatically(List<InventoryProductData> items, List<RackData> racks) {
        for (InventoryProductData item : items) {
            if (item.getRack() != null && !item.getRack().isBlank() && !"-".equals(item.getRack())) {
                continue;
            }

            racks.stream()
                    .filter(rackData -> checkRackSuitability(rackData, item))
                    .findFirst()
                    .ifPresent(rackData -> {
                        assignmentItemToRack(item, rackData);
                        updateProductRack(item.getId(), rackData.getId());
                    });
        }
    }

    @Transactional
    public void itemListInRacksUpdate(Integer productId, Integer rackId) {
        InventoryProductData productData = itemVolumeRequest(productId);
        RackData rackData = rackDataRequest().stream()
                .filter(rack -> rack.getId().equals(rackId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stelažas nerastas"));

        assignmentItemToRack(productData, rackData);
        dataRackSave(rackData);
        dataItemUpdate(productData);
        updateProductRack(productId, rackId);
    }

    public void itemListInRacksRevert() {
        throw new RuntimeException("Prekės išdėstymo pakeitimai atšaukti");
    }

    public List<InventoryProductData> selectItemRequest() {
        return itemListRequest();
    }

    public List<RackData> userChoiceRequest(Integer productId) {
        InventoryProductData productData = itemVolumeRequest(productId);
        return rackListRequest().stream()
                .filter(rackData -> checkRackSuitability(rackData, productData))
                .toList();
    }

    @Transactional
    public void rackChoiceRequest(Integer productId, Integer rackId) {
        chosenItemRack(productId, rackId);
    }

    public void chosenItemRack(Integer productId, Integer rackId) {
        itemListInRacksUpdate(productId, rackId);
    }

    private Optional<Double> readProductNumber(Integer productId, String... candidates) {
        return readProductValue(productId, candidates).map(this::readDouble);
    }

    private Optional<String> readProductText(Integer productId, String... candidates) {
        return readProductValue(productId, candidates).map(this::readText);
    }

    private Optional<Object> readProductValue(Integer productId, String... candidates) {
        List<String> columns = getColumns("Product");
        String column = findColumn(columns, candidates);
        if (column == null) {
            return Optional.empty();
        }
        Object value = jdbcTemplate.queryForObject("select " + column + " from Product where id = ?", Object.class, productId);
        return Optional.ofNullable(value);
    }

    private void updateProductRack(Integer productId, Integer rackId) {
        List<String> columns = getColumns("Product");
        String rackColumn = findColumn(columns, "rack", "shelf", "fk_Shelf", "fk_Rack", "fk_Stelazas", "fk_Stelažas", "stelazas", "stelažas");

        if (rackColumn == null) {
            throw new RuntimeException("Product lentelėje nerastas stelažo laukas");
        }

        jdbcTemplate.update("update Product set " + rackColumn + " = ? where id = ?", rackId, productId);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = database()
                  and table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private List<String> getColumns(String tableName) {
        return jdbcTemplate.queryForList("""
                select column_name
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                order by ordinal_position
                """, String.class, tableName);
    }

    private String findColumn(List<String> columns, String... candidates) {
        Map<String, String> normalizedColumns = columns.stream()
                .collect(Collectors.toMap(this::normalize, column -> column, (first, second) -> first));
        Set<String> keys = normalizedColumns.keySet();

        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            if (keys.contains(normalizedCandidate)) {
                return normalizedColumns.get(normalizedCandidate);
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("_", "").toLowerCase(Locale.ROOT);
    }

    private Double readDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private String readText(Object value) {
        return value == null ? null : value.toString();
    }
}

    private class InventoryData  {
            // private final String baseUrl = 'http://localhost/openmrs/ws/rest/v2/inventory/item';
            private static final String PATIENT_FILES_DIRECTORY = "/Users/yenugukeerthana/Desktop/gok/openmrs-module-openhmis.inventory/inv_item.csv";
  

        private CSVFile getFile(String fileName, String filesDirectory) throws IOException {

        String uploadDirectory = PATIENT_FILES_DIRECTORY;

        system.out.println("uploadDirectory-",uploadDirectory)
        // String relativePath = filesDirectory + fileNameWithoutExtension + timestampForFile + fileExtension;
        FileUtils.forceMkdir(new File(uploadDirectory));
        return new CSVFile(uploadDirectory);
    }

    }
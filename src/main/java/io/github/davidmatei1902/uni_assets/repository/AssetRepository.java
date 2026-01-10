/** Repository JPA care utilizeaza interogari SQL Native
 * @author David Matei
 * @version 10 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.repository;

import io.github.davidmatei1902.uni_assets.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // SQL Pur pentru filtrare (Cerinta E - Nota 10)
    @Query(value = "SELECT * FROM Dotari WHERE NumeDotare LIKE %:name%", nativeQuery = true)
    List<Asset> searchByNameNative(@Param("name") String name);

    // SQL Pur pentru sortare dupa progres (Cerinta E)
    @Query(value = "SELECT * FROM Dotari ORDER BY Progres DESC", nativeQuery = true)
    List<Asset> findAllSortedByProgressNative();
}
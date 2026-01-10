/** Clasa Entity pentru maparea tabelului Dotari si validarea datelor
 * @author David Matei
 * @version 10 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "Dotari")
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DotareID")
    private Long id;

    @NotBlank(message = "Numele este obligatoriu")
    @Column(name = "NumeDotare")
    private String name;

    @Column(name = "TipDotare")
    private String type;

    @Column(name = "Producator") // Schimbat din Cantitate in Producator
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "Stare")
    private AssetStatus status;

    @Column(name = "Progres") // Pastram pentru Cerinta A.2 (poti sa-l pui 0 default)
    private Integer progress = 0;
}
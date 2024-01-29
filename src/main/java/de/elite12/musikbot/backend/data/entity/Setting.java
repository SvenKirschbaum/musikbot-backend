package de.elite12.musikbot.backend.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class Setting {
	@Id
	@Column(length = 100)
	private String name;

	private String value;
}

package de.elite12.musikbot.backend.data.repository;

import de.elite12.musikbot.backend.data.entity.Setting;
import org.springframework.data.repository.CrudRepository;

public interface SettingRepository extends CrudRepository<Setting, String> {

}

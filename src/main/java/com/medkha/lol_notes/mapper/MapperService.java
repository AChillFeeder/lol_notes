package com.medkha.lol_notes.mapper;

import java.util.List;
import java.util.Set;

import com.medkha.lol_notes.dto.FilterSearchRequest;
import com.medkha.lol_notes.dto.interfaces.DeathFilterOption;

public interface MapperService {
    public <D> D convert(Object source, Class<D> destination);
    public <D> Set<D> convertSet(Set source, Class<D> destination);
    public Set<DeathFilterOption> convertFilterSearchRequestToDeathFilterOptions(FilterSearchRequest filterDeathRequest);
    public String mapClassDtoToParamName(Class classDto);
}

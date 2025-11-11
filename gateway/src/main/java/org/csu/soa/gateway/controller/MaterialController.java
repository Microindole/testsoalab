package org.csu.soa.gateway.controller;

import org.csu.soa.gateway.mapper.MaterialMapper;
import org.csu.soa.gateway.model.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MaterialController {

    @Autowired
    private MaterialMapper materialMapper;

    // 1. 获取所有物资
    @GetMapping("/materials")
    public List<Material> getAllMaterials() {
        return materialMapper.selectList(null);
    }

    // 2. 获取单个物资
    @GetMapping("/materials/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        Material material = materialMapper.selectById(id);
        if (material != null) {
            return ResponseEntity.ok(material);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. [核心] 减少库存 (用于借出)
    @PutMapping("/materials/{id}/decrement")
    public ResponseEntity<String> decrementStock(@PathVariable Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            return ResponseEntity.status(404).body("Material not found");
        }
        
        if (material.getQuantity() <= 0) {
            return ResponseEntity.status(400).body("Material out of stock");
        }
        
        // 库存减 1
        material.setQuantity(material.getQuantity() - 1);
        materialMapper.updateById(material);
        
        return ResponseEntity.ok("Stock decremented");
    }
}
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var GroupSchema = new Schema({
	name: String,
	photo: { type: String, default: '' },
	created: { type: Date, default: Date.now },
});

var Group = mongoose.model('group', GroupSchema);

module.exports = Group;